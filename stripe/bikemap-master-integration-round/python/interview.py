import json

# Part 1
def read_json_print_top10_coordinates():
    file_path = 'ride-simple.json'
    with open(file_path, 'r') as file:
        ride_simple_data = json.load(file)
    for i in range(10):
        print(ride_simple_data["features"][0]["geometry"]["coordinates"][i])

# Part2
def create_sample_static_map_image():
    pass

if __name__ == '__main__':
    read_json_print_top10_coordinates()