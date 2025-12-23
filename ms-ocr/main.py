from flask import Flask, request, jsonify
from flask_cors import CORS
import pytesseract
from pdf2image import convert_from_path
from PIL import Image
import re
import os
import tempfile
from datetime import datetime

app = Flask(__name__)
CORS(app)

# ---------------------- HELPERS ----------------------

def clean_number(num_str):
    if not num_str:
        return 0.0
    cleaned = num_str.replace(' ', '').replace(',', '.').replace('\u202f', '')
    try:
        return float(cleaned)
    except:
        return 0.0

def extract_bill_number(text):
    patterns = [
        r'Bill\s*No\.?\s*[:\-]?\s*([\w\d\-]+)',
        r'Facture\s*[:\-]?\s*([\w\d\-]+)',
    ]
    for p in patterns:
        m = re.search(p, text, re.IGNORECASE)
        if m:
            return m.group(1)
    return ''

def extract_date_created(text):
    m = re.search(r'Date\s*[:\-]?\s*(\d{2}/\d{2}/\d{4})', text)
    if m:
        try:
            dt = datetime.strptime(m.group(1), '%d/%m/%Y')
            return dt.strftime('%Y-%m-%d')
        except:
            pass
    return ''

def extract_amount(text):
    m = re.search(r'Amount Due\s*[:\-]?\s*([\d\s,.]+)', text, re.IGNORECASE)
    return clean_number(m.group(1)) if m else 0.0





def extract_description(text):
    m = re.search(r'Description\s*[:\-]?\s*(.+)', text)
    return m.group(1).strip() if m else ''

def extract_status(text):
    m = re.search(r'Status\s*[:\-]?\s*(\w+)', text, re.IGNORECASE)
    return m.group(1).upper() if m else 'PENDING'

def extract_customer_id(text):
    m = re.search(r'Customer\s*ID\s*[:\-]?\s*(\d+)', text)
    return int(m.group(1)) if m else None

# ---------------------- MAIN EXTRACTION ----------------------

def extract_bill(text):
    clean_text = text.replace('\r', '').replace('\t', ' ')
    bill_data = {
        'billNumber': extract_bill_number(clean_text),
        'dateCreated': extract_date_created(clean_text),
        'amount': extract_amount(clean_text),
        'description': extract_description(clean_text),
        # Check for "PAID" in text, otherwise PENDING
        'status': 'PAID' if 'PAID' in clean_text.upper() else 'PENDING',
        'customerId': extract_customer_id(clean_text)
    }
    return bill_data

# ---------------------- FLASK ENDPOINT ----------------------
import requests  # add this at the top with other imports

JAVA_API_URL = "http://localhost:8080/api/bills"

@app.route('/upload-bill', methods=['POST'])
def upload_bill():
    f = request.files.get('file')
    if not f:
        return jsonify({'error': 'No file uploaded'}), 400

    name = f.filename.lower()
    ext = os.path.splitext(name)[1]
    with tempfile.NamedTemporaryFile(delete=False, suffix=ext) as tmp:
        tmp.write(f.read())
        path = tmp.name

    try:
        full_text = ''
        if name.endswith('.pdf'):
            for img in convert_from_path(path, dpi=300):
                full_text += pytesseract.image_to_string(img, lang='fra')
        else:
            img = Image.open(path)
            full_text = pytesseract.image_to_string(img, lang='fra')

        # Extract bill fields
        bill = extract_bill(full_text)

        # Send to Java backend
        bill_payload = {
            "billNumber": bill['billNumber'],
            "dateCreated": bill['dateCreated'],
            "amount": bill['amount'],
            "description": bill['description'],
            "status": bill['status'],
            "customerId": bill['customerId']
        }

        try:
            java_response = requests.post(JAVA_API_URL, json=bill_payload)
            java_result = java_response.json() if java_response.status_code == 200 else {"error": java_response.text}
        except Exception as ex:
            java_result = {"error": str(ex)}

        return jsonify({
            'message': 'Bill processed successfully',
            'bill_data': bill,
            'java_response': java_result,
            'raw_text': full_text
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

    finally:
        try:
            os.remove(path)
        except:
            pass
