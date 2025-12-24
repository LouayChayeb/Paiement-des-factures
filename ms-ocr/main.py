# app.py
from flask import Flask, request, jsonify
from flask_cors import CORS
import pytesseract
from pdf2image import convert_from_path
from PIL import Image
import re
import os
import tempfile
from datetime import datetime
import requests
import logging
from logging.handlers import RotatingFileHandler
import sys

# Initialize Flask app
app = Flask(__name__)
CORS(app)


# ---------------------- CONFIGURATION ----------------------

class Config:
    """Application configuration"""
    SERVICE_NAME = os.getenv('SERVICE_NAME', 'ms-bill-ocr')
    SERVICE_PORT = int(os.getenv('SERVICE_PORT', '5000'))
    SERVICE_HOST = os.getenv('SERVICE_HOST', '0.0.0.0')

    # Java Backend Integration
    JAVA_API_URL = os.getenv('JAVA_API_URL', 'http://localhost:8083/api/bill/bill/addbill')
    JAVA_API_TIMEOUT = int(os.getenv('JAVA_API_TIMEOUT', '10'))

    # Eureka Discovery (if needed)
    EUREKA_SERVER = os.getenv('EUREKA_SERVER', 'http://localhost:8761/eureka')
    REGISTER_WITH_EUREKA = os.getenv('REGISTER_WITH_EUREKA', 'false').lower() == 'true'

    # OCR Configuration
    OCR_LANGUAGE = os.getenv('OCR_LANGUAGE', 'fra')
    PDF_DPI = int(os.getenv('PDF_DPI', '300'))

    # Logging
    LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')

    # File Upload
    MAX_FILE_SIZE = int(os.getenv('MAX_FILE_SIZE', '16777216'))  # 16MB default
    ALLOWED_EXTENSIONS = {'.pdf', '.png', '.jpg', '.jpeg', '.tiff', '.bmp'}


config = Config()


# ---------------------- LOGGING SETUP ----------------------

def setup_logging():
    """Configure application logging"""
    log_format = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )

    # Console handler with UTF-8 encoding
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(log_format)
    # Force UTF-8 encoding for console on Windows
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')

    # File handler with UTF-8 encoding
    if not os.path.exists('logs'):
        os.makedirs('logs')
    file_handler = RotatingFileHandler(
        'logs/ms-bill-ocr.log',
        maxBytes=10485760,  # 10MB
        backupCount=10,
        encoding='utf-8'
    )
    file_handler.setFormatter(log_format)

    # Configure root logger
    app.logger.addHandler(console_handler)
    app.logger.addHandler(file_handler)
    app.logger.setLevel(getattr(logging, config.LOG_LEVEL))


setup_logging()

# ---------------------- EUREKA REGISTRATION (OPTIONAL) ----------------------

import threading
import time

eureka_instance_id = None


def register_with_eureka():
    """Register service with Eureka discovery server"""
    global eureka_instance_id

    if not config.REGISTER_WITH_EUREKA:
        return

    try:
        import socket
        hostname = socket.gethostname()
        ip_address = socket.gethostbyname(hostname)

        # Create unique instance ID
        eureka_instance_id = f"{config.SERVICE_NAME}:{ip_address}:{config.SERVICE_PORT}"

        eureka_payload = {
            "instance": {
                "instanceId": eureka_instance_id,
                "hostName": hostname,
                "app": config.SERVICE_NAME.upper(),
                "ipAddr": ip_address,
                "vipAddress": config.SERVICE_NAME,
                "status": "UP",
                "port": {
                    "$": config.SERVICE_PORT,
                    "@enabled": "true"
                },
                "healthCheckUrl": f"http://{ip_address}:{config.SERVICE_PORT}/actuator/health",
                "statusPageUrl": f"http://{ip_address}:{config.SERVICE_PORT}/actuator/info",
                "homePageUrl": f"http://{ip_address}:{config.SERVICE_PORT}/",
                "dataCenterInfo": {
                    "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                    "name": "MyOwn"
                },
                "leaseInfo": {
                    "renewalIntervalInSecs": 30,
                    "durationInSecs": 90
                },
                "metadata": {
                    "management.port": str(config.SERVICE_PORT)
                }
            }
        }

        # Register with Eureka
        response = requests.post(
            f"{config.EUREKA_SERVER}/apps/{config.SERVICE_NAME.upper()}",
            json=eureka_payload,
            headers={"Content-Type": "application/json"},
            timeout=5
        )

        if response.status_code in [200, 204]:
            app.logger.info(f"[OK] Registered with Eureka: {config.EUREKA_SERVER}")
            app.logger.info(f"Instance ID: {eureka_instance_id}")

            # Start heartbeat thread
            heartbeat_thread = threading.Thread(target=send_heartbeat, daemon=True)
            heartbeat_thread.start()
        else:
            app.logger.warning(f"Eureka registration failed: {response.status_code} - {response.text}")

    except Exception as e:
        app.logger.warning(f"Could not register with Eureka: {str(e)}")


def send_heartbeat():
    """Send periodic heartbeat to Eureka"""
    global eureka_instance_id

    while True:
        try:
            time.sleep(30)  # Send heartbeat every 30 seconds

            if eureka_instance_id:
                response = requests.put(
                    f"{config.EUREKA_SERVER}/apps/{config.SERVICE_NAME.upper()}/{eureka_instance_id}",
                    timeout=5
                )

                if response.status_code == 200:
                    app.logger.debug("Heartbeat sent to Eureka")
                else:
                    app.logger.warning(f"Heartbeat failed: {response.status_code}")

        except Exception as e:
            app.logger.error(f"Heartbeat error: {str(e)}")


# ---------------------- HELPER FUNCTIONS ----------------------

def clean_number(num_str):
    """Clean and convert a number string to float."""
    if not num_str:
        return 0.0
    cleaned = num_str.replace(' ', '').replace(',', '.').replace('\u202f', '')
    try:
        return float(cleaned)
    except:
        return 0.0


def extract_bill_number(text):
    """Extract bill number from text."""
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
    """Extract and format date from text."""
    m = re.search(r'Date\s*[:\-]?\s*(\d{2}/\d{2}/\d{4})', text)
    if m:
        try:
            dt = datetime.strptime(m.group(1), '%d/%m/%Y')
            return dt.strftime('%Y-%m-%d')
        except:
            pass
    return ''


def extract_amount(text):
    """Extract amount due from text."""
    m = re.search(r'Amount Due\s*[:\-]?\s*([\d\s,.]+)', text, re.IGNORECASE)
    return clean_number(m.group(1)) if m else 0.0


def extract_description(text):
    """Extract description from text."""
    m = re.search(r'Description\s*[:\-]?\s*(.+)', text)
    return m.group(1).strip() if m else ''


def extract_status(text):
    """Extract or infer status from text."""
    return 'PAID' if 'PAID' in text.upper() else 'PENDING'


def extract_customer_id(text):
    """Extract customer ID from text."""
    m = re.search(r'Customer\s*ID\s*[:\-]?\s*(\d+)', text)
    return int(m.group(1)) if m else None


def extract_bill(text):
    """Extract all bill information from OCR text."""
    clean_text = text.replace('\r', '').replace('\t', ' ')
    bill_data = {
        'bill_number': extract_bill_number(clean_text),
        'date_created': extract_date_created(clean_text),
        'amount': extract_amount(clean_text),
        'description': extract_description(clean_text),
        'status': extract_status(clean_text),
        'customer_id': extract_customer_id(clean_text)
    }
    return bill_data


# ---------------------- ACTUATOR ENDPOINTS (Spring-like) ----------------------

@app.route('/actuator/health', methods=['GET'])
def health():
    """Health check endpoint - mimics Spring Boot Actuator"""
    try:
        # Check Tesseract
        pytesseract.get_tesseract_version()
        tesseract_status = "UP"
    except:
        tesseract_status = "DOWN"

    overall_status = "UP" if tesseract_status == "UP" else "DOWN"

    return jsonify({
        "status": overall_status,
        "components": {
            "tesseract": {
                "status": tesseract_status
            },
            "diskSpace": {
                "status": "UP"
            }
        }
    }), 200 if overall_status == "UP" else 503


@app.route('/actuator/info', methods=['GET'])
def info():
    """Info endpoint - mimics Spring Boot Actuator"""
    return jsonify({
        "app": {
            "name": config.SERVICE_NAME,
            "description": "Bill OCR Processing Microservice",
            "version": "1.0.0",
            "language": "Python 3.x",
            "framework": "Flask"
        }
    })


@app.route('/', methods=['GET'])
def home():
    """Root endpoint"""
    return jsonify({
        'service': config.SERVICE_NAME,
        'status': 'running',
        'version': '1.0.0',
        'endpoints': {
            'health': '/actuator/health',
            'info': '/actuator/info',
            'upload': '/api/bill/upload'
        }
    })


# ---------------------- BUSINESS ENDPOINTS ----------------------

@app.route('/api/bill/upload', methods=['POST'])
def upload_bill():
    """Process uploaded bill image/PDF and extract information."""
    app.logger.info("Received bill upload request")

    # Check for file
    f = request.files.get('file')
    if not f:
        app.logger.warning("No file in request")
        return jsonify({'error': 'No file uploaded'}), 400

    name = f.filename.lower()
    ext = os.path.splitext(name)[1]

    # Validate file type
    if ext not in config.ALLOWED_EXTENSIONS:
        app.logger.warning(f"Invalid file type: {ext}")
        return jsonify({
            'error': f'File type not supported. Allowed: {", ".join(config.ALLOWED_EXTENSIONS)}'
        }), 400

    # Save uploaded file temporarily
    with tempfile.NamedTemporaryFile(delete=False, suffix=ext) as tmp:
        tmp.write(f.read())
        path = tmp.name

    try:
        app.logger.info(f"Processing file: {name}")
        full_text = ''

        # Process PDF or image
        if name.endswith('.pdf'):
            images = convert_from_path(path, dpi=config.PDF_DPI)
            for img in images:
                full_text += pytesseract.image_to_string(img, lang=config.OCR_LANGUAGE) + '\n'
        else:
            img = Image.open(path)
            full_text = pytesseract.image_to_string(img, lang=config.OCR_LANGUAGE)

        # Extract bill fields
        bill = extract_bill(full_text)
        app.logger.info(f"Extracted bill: {bill['bill_number']}")

        # Prepare payload for Java backend
        bill_payload = {
            "bill_number": bill['bill_number'],
            "date_created": bill['date_created'],
            "amount": bill['amount'],
            "description": bill['description'],
            "status": bill['status'],
            "customer_id": bill['customer_id']
        }

        # Send to Java backend
        java_result = {}
        try:
            app.logger.info(f"Sending to Java API: {config.JAVA_API_URL}")
            java_response = requests.post(
                config.JAVA_API_URL,
                json=bill_payload,
                timeout=config.JAVA_API_TIMEOUT
            )
            if java_response.status_code == 200:
                java_result = java_response.json()
                app.logger.info("Successfully sent to Java API")
            else:
                java_result = {
                    "error": f"Java API returned status {java_response.status_code}",
                    "details": java_response.text
                }
                app.logger.error(f"Java API error: {java_response.status_code}")
        except requests.exceptions.ConnectionError:
            java_result = {"error": "Could not connect to Java API. Is it running?"}
            app.logger.error("Java API connection failed")
        except requests.exceptions.Timeout:
            java_result = {"error": "Java API request timed out"}
            app.logger.error("Java API timeout")
        except Exception as ex:
            java_result = {"error": f"Java API error: {str(ex)}"}
            app.logger.error(f"Java API exception: {str(ex)}")

        return jsonify({
            'success': True,
            'message': 'Bill processed successfully',
            'data': bill,
            'javaResponse': java_result,
            'rawText': full_text if request.args.get('includeRaw') == 'true' else None
        })

    except Exception as e:
        app.logger.error(f"Processing error: {str(e)}", exc_info=True)
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

    finally:
        # Clean up temporary file
        try:
            os.remove(path)
        except:
            pass


# ---------------------- ERROR HANDLERS ----------------------

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Endpoint not found'}), 404


@app.errorhandler(500)
def internal_error(error):
    return jsonify({'error': 'Internal server error'}), 500


# ---------------------- APPLICATION STARTUP ----------------------

if __name__ == '__main__':
    # Set UTF-8 encoding for Windows console
    if sys.platform == 'win32':
        import codecs

        sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')
        sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer, 'strict')

    # ASCII Art Banner
    banner = """
    ================================================
         MS-BILL-OCR Microservice v1.0.0
         Bill Processing & OCR Service
    ================================================
    """
    print(banner)

    # Check Tesseract availability
    try:
        version = pytesseract.get_tesseract_version()
        app.logger.info(f"[OK] Tesseract OCR {version} is available")
    except:
        app.logger.error("[ERROR] Tesseract OCR not found!")
        app.logger.error("  Install instructions:")
        app.logger.error("  - Ubuntu/Debian: sudo apt-get install tesseract-ocr tesseract-ocr-fra")
        app.logger.error("  - macOS: brew install tesseract tesseract-lang")
        app.logger.error("  - Windows: https://github.com/UB-Mannheim/tesseract/wiki")
        sys.exit(1)

    # Register with Eureka if enabled
    if config.REGISTER_WITH_EUREKA:
        register_with_eureka()

    # Print configuration
    app.logger.info(f"Service Name: {config.SERVICE_NAME}")
    app.logger.info(f"Service Port: {config.SERVICE_PORT}")
    app.logger.info(f"Java API URL: {config.JAVA_API_URL}")
    app.logger.info(f"Eureka Registration: {config.REGISTER_WITH_EUREKA}")
    app.logger.info("")
    app.logger.info("Available Endpoints:")
    app.logger.info("  GET  /                    - Service info")
    app.logger.info("  GET  /actuator/health     - Health check")
    app.logger.info("  GET  /actuator/info       - Service info")
    app.logger.info("  POST /api/bill/upload     - Upload and process bill")
    app.logger.info("")
    app.logger.info(f"[STARTING] Server on http://{config.SERVICE_HOST}:{config.SERVICE_PORT}")

    # Run the application
    app.run(
        host=config.SERVICE_HOST,
        port=config.SERVICE_PORT,
        debug=False  # Set to False in production
    )