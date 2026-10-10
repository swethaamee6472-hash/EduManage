import cv2
import sys

def main():
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("ERROR: Cannot open camera", file=sys.stderr)
        sys.exit(1)
        
    detector = cv2.QRCodeDetector()
    
    print("READY", flush=True)

    while True:
        ret, frame = cap.read()
        if not ret:
            break
            
        cv2.imshow("EduManage Live QR Scanner - Press ESC to close", frame)
        
        data, bbox, _ = detector.detectAndDecode(frame)
        if data:
            print(data, flush=True)
            break
            
        if cv2.waitKey(1) == 27: # ESC key
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
