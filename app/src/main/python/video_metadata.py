import yt_dlp


def get_info(video_url):
    ydl_opts = {
        'format': 'best',  # Set desired video quality
        'geturl': True,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        try:
            result = ydl.extract_info(video_url, download=False)
            video_info = {
                "title": result.get('title', ''),
                "likes": result.get('like_count', 0),
                "views": result.get('view_count', 0),
                "thumbnail": result.get('thumbnail', ''),
                "url": result.get('url', '')
            }
            return video_info
        except yt_dlp.DownloadError as e:
            print(f"Error: {str(e)}")
