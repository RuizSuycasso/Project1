import cv2
import pytesseract
import numpy as np
import rotate_func

def process_image(image_bytes):
    # Chuyển bytes về numpy array
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    # Xoay ảnh nếu cần
    fixed = rotate_func.deskew(img, angle_threshold=1)

    # Chuyển về grayscale
    gray = cv2.cvtColor(fixed, cv2.COLOR_RGB2GRAY)

    # Resize để cải thiện OCR
    high_res_image = cv2.resize(gray, None, fx=2, fy=2, interpolation=cv2.INTER_CUBIC)

    # Adaptive threshold
    adaptive_thresh = cv2.adaptiveThreshold(high_res_image, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 101, 21)

    # Nhận diện văn bản bằng OCR
    pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'
    custom_config = r'--oem 3 --psm 6'
    text = pytesseract.image_to_string(adaptive_thresh, lang="vie+viehand", config=custom_config)

    return text
