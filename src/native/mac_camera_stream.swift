import AVFoundation
import CoreImage
import Foundation

// EduManage Native macOS Apple Silicon Camera Capture
// Streams JPEG frames with a 4-byte big-endian length prefix to stdout.
class CameraStreamer: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate {
    let session = AVCaptureSession()
    let context = CIContext(options: [.useSoftwareRenderer: false])
    let colorSpace = CGColorSpace(name: CGColorSpace.sRGB)!
    var lastFrameTime: Double = 0
    let frameInterval: Double = 0.05 // ~20 FPS

    func start() {
        session.sessionPreset = .vga640x480

        guard let device = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: device) else {
            fputs("ERROR: No camera device available\n", stderr)
            exit(1)
        }
        if session.canAddInput(input) {
            session.addInput(input)
        }

        let output = AVCaptureVideoDataOutput()
        output.alwaysDiscardsLateVideoFrames = true
        output.videoSettings = [kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA]
        output.setSampleBufferDelegate(self, queue: DispatchQueue(label: "camera_stream_queue", qos: .userInteractive))
        if session.canAddOutput(output) {
            session.addOutput(output)
        }

        session.startRunning()
        fputs("READY\n", stderr)
    }

    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        let now = Date().timeIntervalSince1970
        if now - lastFrameTime < frameInterval {
            return
        }
        lastFrameTime = now

        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
        guard let jpegData = context.jpegRepresentation(of: ciImage, colorSpace: colorSpace, options: [kCGImageDestinationLossyCompressionQuality as CIImageRepresentationOption: 0.75]) else {
            return
        }

        var length = UInt32(jpegData.count).bigEndian
        withUnsafeBytes(of: &length) { ptr in
            FileHandle.standardOutput.write(Data(ptr))
        }
        FileHandle.standardOutput.write(jpegData)
    }
}

// Exit when stdin closes (Java process terminates or stops scanner)
DispatchQueue.global(qos: .background).async {
    let _ = FileHandle.standardInput.readDataToEndOfFile()
    exit(0)
}

let streamer = CameraStreamer()
streamer.start()

RunLoop.main.run()
