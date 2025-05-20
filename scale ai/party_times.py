
def get_party_time_slots_for_neighbourhoods(party_info, geo_info):
    party_info_dict = {}
    for (id, st, en) in party_info:
        party_info_dict[id] = (st, en)
    
    party_time_slots = {}
    for (nbd, _, _, party_id) in geo_info:
        if nbd in party_time_slots:
            party_time_slots[nbd].append(party_info_dict[party_id])
        else:
            party_time_slots[nbd] = [party_info_dict[party_id]]
    return party_time_slots
    
def get_party_time_blocks(party_time_slots):
    party_time_blocks = {}
    deadzones = {}
    for neighbourhood in party_time_slots.keys():
        time_block_st, time_block_end, deadzone = get_merged_time_block(party_time_slots[neighbourhood])
        party_time_blocks[neighbourhood] = (time_block_st, time_block_end)
        deadzones[neighbourhood] = deadzone
    return party_time_blocks, deadzones

def get_merged_time_block(time_slots):
    sorted_intervals = sorted(time_slots)
    merged_intervals = [sorted_intervals[0]]
    time_block_st = sorted_intervals[0][0]
    time_block_en = sorted_intervals[0][1]

    for i in range(1, len(sorted_intervals)):
        time_block_st = min(time_block_st, sorted_intervals[i][0])
        time_block_en = max(time_block_en, sorted_intervals[i][1])
        if sorted_intervals[i][0] <= merged_intervals[-1][1]:
            updated_interval = (merged_intervals[-1][0], max(merged_intervals[-1][1], sorted_intervals[i][1]))
            merged_intervals.pop()
            merged_intervals.append(updated_interval)
        else:
            merged_intervals.append(sorted_intervals[i])

    deadzone = 0
    for i in range(1, len(merged_intervals)):
        deadzone += merged_intervals[i][0] - merged_intervals[i-1][1]
    return time_block_st, time_block_en, deadzone

if __name__ == '__main__':
    party_info = [
        (1, 10, 12),
        (2, 13, 16),
        (3, 18, 20),
        (4, 9, 11),
        (5, 21, 22),
        (6, 23, 26),
        (7, 8, 10),
        (8, 14, 17),
        (9, 28, 30),
        (10, 32, 36)
    ]

    geo_info = [
        ("Northside", "Metropolis", "NY", 1),
        ("Northside", "Metropolis", "NY", 2),
        ("Northside", "Metropolis", "NY", 3),  # → blocks: (10,12), (13,16), (18,20)

        ("East End", "Metropolis", "NY", 4),
        ("East End", "Metropolis", "NY", 5),
        ("East End", "Metropolis", "NY", 6),   # → blocks: (9,11), (21,22), (23,26)

        ("Riverside", "Gotham", "NJ", 7),
        ("Riverside", "Gotham", "NJ", 8),
        ("Riverside", "Gotham", "NJ", 9),
        ("Riverside", "Gotham", "NJ", 10)      # → blocks: (8,10), (14,17), (28,30), (32,36)
    ]
    party_time_slots = get_party_time_slots_for_neighbourhoods(party_info, geo_info)
    party_time_blocks, deadzones = get_party_time_blocks(party_time_slots)
    print(party_time_blocks)
    print(deadzones)