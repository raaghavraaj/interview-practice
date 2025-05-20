class DeliverySystem:
    def __init__(self, data_menu, data_locations):
        self.locations = {}
        for location in data_locations:
            self.locations[location[0]] = (location[1], location[2])
        
        self.menus = {}
        for menu in data_menu:
            if menu[0] not in self.menus:
                menu

if __name__ == '__main__':
    data_menu = [(1, 'Burger', 20), (1, 'Sandwich', 20), (2, 'Burger', 25), (2, 'Sandwich', 15)]
    data_locations = [(1, 8, 10), (2, 11, 14)]
    ds = DeliverySystem(data_menu, data_locations)
    pass