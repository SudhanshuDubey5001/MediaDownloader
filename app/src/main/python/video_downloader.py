import yt_dlp

def download_video(video_url, output_path):
    ydl_opts = {
        'format': 'best',  # Set desired video quality
        'outtmpl': output_path
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        try:
            ydl.download([video_url])
        except yt_dlp.DownloadError as e:
            print(f"Error: {str(e)}")