"use client";

import React, { useEffect, useRef, useState } from 'react'
import { formatDateTimeUTC } from './utils/datetime';

const RecorderState = {
  INACTIVE: "inactive",
  RECORDING: "recording",
  PAUSED: "paused"
} as const;

const VIDEO_BITRATE = parseInt(process.env.NEXT_PUBLIC_VIDEO_BITRATE || "3750000", 10);
const AUDIO_BITRATE = parseInt(process.env.NEXT_PUBLIC_AUDIO_BITRATE || "128000", 10);
const FRAME_RATE = parseInt(process.env.NEXT_PUBLIC_FRAME_RATE || "30", 10);

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
              const canvas = refCanvas.current!;
              ctx.drawImage(refVideo.current, 0, 0, canvas.width, canvas.height);

              ctx.save();
              ctx.globalAlpha = 0.08;
              const logoWidth = 400;
              const logoHeight = 160;
              const x1 = (canvas.width - logoWidth) / 2;
              const y1 = (canvas.height - logoHeight) / 2;
              ctx.drawImage(logo, x1, y1, logoWidth, logoHeight);
              ctx.restore();

              ctx.save();
              const padding = 10;
              const fontSize = 14;
              ctx.font = `bold ${fontSize}px sans-serif`;
              ctx.textAlign = "right";
              ctx.textBaseline = "top";

              ctx.globalAlpha = 0.45;
              ctx.fillStyle = "rgba(255, 255, 255, 1)";
              ctx.shadowColor = "rgba(0,0,0,0.6)";
              ctx.shadowOffsetX = 1;
              ctx.shadowOffsetY = 1;
              ctx.shadowBlur = 2;

              const x2 = (canvas.width / (window.devicePixelRatio || 1)) - padding;
              const y2 = padding;
              ctx.fillText(formatDateTimeUTC(), x2, y2);

              ctx.restore();
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
      const stream = refCanvas.current.captureStream(FRAME_RATE);

      if (!stream) {
        console.warn("No media stream found — cannot start recording.");
        return;
      }

      const audioStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      const audioTrack = audioStream.getTracks()[0];

      if (audioTrack) {
        stream.addTrack(audioTrack);
      }

      refMediaRecorder.current = new MediaRecorder(
        stream,
        {
          mimeType: "video/webm;codecs=vp9,opus",
          videoBitsPerSecond: VIDEO_BITRATE,
          audioBitsPerSecond: AUDIO_BITRATE
        }
      );

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