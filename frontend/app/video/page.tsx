"use client";

import { useEffect, useRef, useState } from "react";
import VideoService from "../services/video-service";

export default function Page() {
    const videoRef = useRef<HTMLVideoElement>(null);
    const [url, setUrl] = useState<string | null | undefined>(undefined);

    const fileName = "2.webm";
    const refreshDuration = 10000; // refresh presigned URL before it expires

    const fetchUrl = async (): Promise<string | null> => {
        try {
            const uri = await VideoService.getVideoUri(fileName);
            console.log("Fetched new presigned URL:", uri);
            return uri || null;
        } catch (err) {
            console.error("Failed to fetch video URI", err);
            return null;
        }
    };

    useEffect(() => {
        let interval: NodeJS.Timeout;

        const init = async () => {
            const initialUrl = await fetchUrl();
            setUrl(initialUrl); // set state immediately

            if (!initialUrl) return; // stop if unauthorized

            // Wait until videoRef.current exists
            const videoEl = videoRef.current;
            if (!videoEl) return;

            videoEl.src = initialUrl;
            videoEl.muted = true; // allow autoplay
            videoEl.load();

            videoEl.addEventListener(
                "loadedmetadata",
                () => {
                    videoEl.play().catch(() => { });
                },
                { once: true }
            );

            // Refresh presigned URL periodically
            interval = setInterval(async () => {
                const newUrl = await fetchUrl();
                if (!newUrl || !videoEl) return;

                const currentTime = videoEl.currentTime;
                const wasPaused = videoEl.paused;

                videoEl.src = newUrl;
                videoEl.currentTime = currentTime;
                videoEl.load();

                if (!wasPaused) {
                    videoEl.play().catch(() => { });
                }

                setUrl(newUrl);
            }, refreshDuration);
        };

        init();

        return () => clearInterval(interval);
    }, []);

    return (
        <div className="flex flex-col items-center p-6">
            <h1 className="text-xl font-bold mb-4">Video Player</h1>

            {url === undefined ? (
                <p>Loading video...</p>
            ) : url === null ? (
                <p className="text-red-500">Video unavailable or unauthorized</p>
            ) : (
                <video
                    ref={videoRef}
                    width="640"
                    height="360"
                    controls
                    className="rounded-lg shadow-md"
                    preload="metadata"
                    src={url ?? undefined}
                    onContextMenu={(e) => e.preventDefault()}
                />
            )}
        </div>
    );
}
