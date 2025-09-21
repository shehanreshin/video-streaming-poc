"use client";

import React, { useEffect, useRef, useState } from 'react'

const RecorderState = {
  INACTIVE: "inactive",
  RECORDING: "recording",
  PAUSED: "paused"
} as const;

const page = () => {
  const refVideo = useRef<HTMLVideoElement>(null);
  const refMediaRecorder = useRef<MediaRecorder | null>(null);
  const refChunks = useRef<Blob[]>([]);
  const refCanvas = useRef<HTMLCanvasElement>(null);
  const [recordedVideo, setRecordedVideo] = useState<string | null>(null);
  const [recording, setRecording] = useState<boolean>(false);

  useEffect(() => {
    async function setupCamera() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        console.log("Tracks:", stream.getTracks());
        if (refVideo.current) {
          refVideo.current.srcObject = stream;
        }

        const ctx = refCanvas.current?.getContext("2d");
        const logo = new Image();
        logo.src = "/logo.png"; // 👈 Your company logo path
        logo.onload = () => {
          const drawFrame = () => {
            if (ctx && refVideo.current) {
              // Draw camera feed
              ctx.drawImage(refVideo.current, 0, 0, refCanvas.current!.width, refCanvas.current!.height);


              ctx.globalAlpha = 0.08;
              const logoWidth = 400;
              const logoHeight = 160;
              const x = (refCanvas.current!.width - logoWidth) / 2;
              const y = (refCanvas.current!.height - logoHeight) / 2;
              ctx.drawImage(logo, x, y, logoWidth, logoHeight);
              ctx.globalAlpha = 1.0;
            }
            requestAnimationFrame(drawFrame);
          };
          drawFrame();
        };
      } catch (err) {
        console.error("Error accessing camera: ", err)
      }
    }
    setupCamera();
  }, []);

  const startRecording = async () => {
    try {
      if (!refCanvas.current) return;

      refChunks.current = []
      const stream = refCanvas.current.captureStream(30);

      if (!stream) {
        console.warn("No media stream found — cannot start recording.");
        return;
      }

      const audioStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      const audioTrack = audioStream.getTracks()[0];

      if (audioTrack) {
        stream.addTrack(audioTrack);
      }

      refMediaRecorder.current = new MediaRecorder(stream, { mimeType: "video/webm;codecs=vp9,opus" });

      refMediaRecorder.current.ondataavailable = (event) => {
        if (event.data.size > 0) refChunks.current.push(event.data);
      }

      refMediaRecorder.current.onstop = () => {
        if (refChunks.current.length === 0) {
          console.warn("No video chunks were recorded.");
          return;
        }

        const videoURL = URL.createObjectURL(new Blob(refChunks.current, { type: "video/webm" }));
        refChunks.current = [];

        if (recordedVideo) URL.revokeObjectURL(recordedVideo);
        setRecordedVideo(videoURL);
      }

      console.log("Video URL: ", recordedVideo);

      refMediaRecorder.current.start(500);
      setRecording(true);
    } catch (err) {
      console.error("Failed to start recording: ", err)
    }
  }

  const stopRecording = () => {
    if (refMediaRecorder.current && refMediaRecorder.current?.state !== RecorderState.INACTIVE) {
      refMediaRecorder.current.requestData();
      refMediaRecorder.current.stop();
      setRecording(false);
    }
  }

  return (
    <div className="w-full h-[100vh] flex flex-col items-center gap-10 p-6 bg-cyan-100">
      <h1 className="text-2xl font-bold">Camera Recorder</h1>

      <video ref={refVideo} autoPlay playsInline muted className="w-[50vw] h-[50vh]" />
      <canvas className='hidden' ref={refCanvas} width={640} height={480} style={{ border: "1px solid black" }} />

      <div className="flex gap-4">
        {!recording ? (
          <button onClick={startRecording} className="px-4 py-2 bg-green-600 text-white rounded-lg">
            Start Recording
          </button>
        ) : (
          <button onClick={stopRecording} className="px-4 py-2 bg-red-600 text-white rounded-lg">
            Stop Recording
          </button>
        )}
      </div>

      {/* Recorded video playback + download */}
      {recordedVideo && (
        <div className="mt-6">
          <h2 className="text-lg font-semibold">Recorded Video:</h2>
          <video src={recordedVideo} controls className="w-[400px] h-[300px] rounded-lg" />
          <a
            href={recordedVideo}
            download="recorded-video.webm"
            className="block mt-2 px-4 py-2 bg-blue-600 text-white rounded-lg text-center"
          >
            Download Video
          </a>
        </div>
      )}
    </div>
  )
}

export default page