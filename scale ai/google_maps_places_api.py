import requests

URL = "https://maps.googleapis.com/maps/api/place/textsearch/json"
API_KEY = ""

    
def fetch_top_restaurants(southwest: tuple, northeast: tuple, cuisines = []):
    bounds = f"{southwest[0]},{southwest[1]}|{northeast[0]},{northeast[1]}"
    params = {
        "query": "restaurants", 
        "bounds": bounds,
        "key": API_KEY
    }

if __name__ == '__main__':
    pass