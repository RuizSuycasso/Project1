def getSkewAngle(cvImage):
    try:
        import cv2
        import numpy as np

        newImage = cvImage.copy()
        gray = cv2.cvtColor(newImage, cv2.COLOR_BGR2GRAY)
        blur = cv2.GaussianBlur(gray, (9, 9), 0)
        thresh = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

        kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (30, 5))
        dilate = cv2.dilate(thresh, kernel, iterations=2)

        contours, hierarchy = cv2.findContours(dilate, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
        if not contours:
            return 0.0

        contours = sorted(contours, key=cv2.contourArea, reverse=True)
        largestContour = contours[0]
        minAreaRect = cv2.minAreaRect(largestContour)

        angle = minAreaRect[-1]
        if angle < -45:
            angle = 90 + angle
        elif angle > 45:
            angle = angle - 90

        return -1.0 * angle
    except:
        return 0.0

def rotateImage(cvImage, angle):
    try:
        import cv2
        newImage = cvImage.copy()
        (h, w) = newImage.shape[:2]
        center = (w // 2, h // 2)
        M = cv2.getRotationMatrix2D(center, angle, 1.0)
        newImage = cv2.warpAffine(newImage, M, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
        return newImage
    except:
        return cvImage

def deskew(cvImage, angle_threshold=1.0):
    try:
        angle = getSkewAngle(cvImage)
        if abs(angle) < angle_threshold:
            return cvImage
        return rotateImage(cvImage, -1.0 * angle)
    except:
        return cvImage