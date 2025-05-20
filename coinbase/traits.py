import random

RARITY_WEIGHTS = {
    "common": 0.7,
    "uncommon": 0.25,
    "rare": 0.05
}

properties = {
    "Length": [("Short", "common"), ("Medium", "common"), ("Tall", "rare")],
    "Leaf Colour": [("Green", "common"), ("Yellow", "rare"), ("Red", "common")]
}

limits = {
    "Length": {"Short": 1, "Medium": 1, "Tall": 2},
    "Leaf Colour": {"Green": 1, "Yellow": 1, "Red": 2}
}

def weighted_choice(choices):
    values = [val for val, _ in choices]
    weights = [RARITY_WEIGHTS[rarity] for _, rarity in choices]
    return random.choices(values, weights=weights)[0]

def generate_object(properties):
    obj = {}
    for prop, choices in properties.items():
        obj[prop] = weighted_choice(choices)
    return obj

def generate_objects_with_limits(properties, generation_limits, num_objects):
    generated = []
    for _ in range(num_objects):
        obj = {}
        for prop, options in properties.items():
            available = [v for v in options if generation_limits[prop][v[0]] > 0]
            if not available:
                break  # Skip this object if no options left for a property
            value = random.choice(available)
            obj[prop] = value[0]
            generation_limits[prop][value[0]] -= 1
        else:
            generated.append(obj)  # only add if all props were successfully assigned
    return generated

if __name__ == '__main__':
    random.seed(42)
    unique_objs = generate_objects_with_limits(properties, limits, 10)
    for obj in unique_objs:
        print(obj)
