import bisect

class GleanTable:
    def __init__(self, column_names: list[str]):
        self.data = {}
        self.len_table = 0
        for col in column_names:
            self.data[col] = []

    def append_row(self, row: list[int]):
        self.len_table += 1
        for key, row_val in zip(self.data.keys(), row):
            self.data[key].append(row_val)
    
    def print(self, columns: list[str]):
        if len(columns) == 0:
            columns = list(self.data.keys())
        for col in columns:
            print(col, end=' ')
        print()
        
        for i in range(len(self.data[columns[0]])):
            for col in columns:
                print(self.data[col][i], end=' ')
            print()
    
    def slice(self, columns: list[str]) -> 'GleanTable':
        sliced_table = GleanTable(columns)
        for i in range(len(self.data[columns[0]])):
            row = []
            for col in columns:
                row.append(self.data[col][i])
            sliced_table.append_row(row)
        return sliced_table
    
    def add_column(self, column: str) ->  'GleanTable':
        self.data[column] = [0] * self.len_table
        return self
    
    def delete_column(self, column: str) -> 'GleanTable':
        del self.data[column]
        return self
    
    def inner_join(self, table2: 'GleanTable', column: str) -> 'GleanTable':
        # Generating final columns list 
        data1 = self.data
        data2 = table2.__get_data()
        columns1 = list(data1.keys())
        columns1.remove(column) 
        columns2 = list(data2.keys())
        columns2.remove(column)
        final_columns = []
        for col in columns1:
            final_columns.append(col)
        final_columns.append(column)
        for col in columns2:
            final_columns.append(col)

        inner_joined_table = GleanTable(final_columns)
        indexed_vals_table2_sorted = sorted(list(enumerate(data2[column])), key=lambda x: x[1])
        for idx1, val in enumerate(data1[column]):
            idx2 = self.__find_value_in_table(indexed_vals_table2_sorted, val)
            if idx2 != -1:
                row = []
                for col in columns1:
                    row.append(data1[col][idx1])
                row.append(val)
                for col in columns2:
                    row.append(data2[col][idx2])
                inner_joined_table.append_row(row)
        return inner_joined_table        
    
    def outer_join(self, table2: 'GleanTable', column: str) -> 'GleanTable':
        # Generating final columns list 
        data1 = self.data
        data2 = table2.__get_data()
        columns1 = list(data1.keys())
        columns1.remove(column) 
        columns2 = list(data2.keys())
        columns2.remove(column)
        final_columns = []
        for col in columns1:
            final_columns.append(col)
        final_columns.append(column)
        for col in columns2:
            final_columns.append(col)

        outer_joined_table = GleanTable(final_columns)

        for idx1, val1 in enumerate(data1[column]):
            matching_idx_table_2 = []
            for idx2, val2 in enumerate(data2[column]):
                if val1 == val2:
                    matching_idx_table_2.append(idx2)
            if len(matching_idx_table_2) == 0:
                row = []
                for col in columns1:
                    row.append(data1[col][idx1])
                row.append(val1)
                for col in columns2:
                    row.append(0)
                outer_joined_table.append_row(row)
            else:
                for idx2 in matching_idx_table_2:
                    row = []
                    for col in columns1:
                        row.append(data1[col][idx1])
                    row.append(val1)
                    for col in columns2:
                        row.append(data2[col][idx2])
                    outer_joined_table.append_row(row)

        for idx2, val2 in enumerate(data2[column]):
            found = False
            for idx1, val1 in enumerate(data1[column]):
                if val1 == val2:
                    found = True
                    break
            if found:
                continue

            row = [0]*len(columns1)
            row.append(val2)
            for col in columns2:
                row.append(data2[col][idx2])
            outer_joined_table.append_row(row)

        return outer_joined_table
    
    def __get_data(self):
        return self.data
    
    def __find_value_in_table(self, sorted_arr, target):
        values = [val for idx, val in sorted_arr]
        idx = bisect.bisect_left(values, target)
        if idx < len(values) and values[idx] == target:
            return sorted_arr[idx][0]
        else:
            return -1
    


if __name__ == '__main__':
    gt = GleanTable(['A', 'B', 'C'])

    # V0
    gt.append_row([1, 3, 9])
    gt.append_row([2, 6, 18])
    gt.print(['A', 'B', 'C'])
    gt.print(['B', 'C'])
    gt.print([])
    print()

    # V1
    sliced_table_1 = gt.slice(['B'])
    sliced_table_1.append_row([9])
    sliced_table_1.print([])
    print()
    sliced_table_2 = gt.slice(['A', 'C'])
    sliced_table_2.append_row([3, 27])
    sliced_table_2.print([])
    print()

    # V2
    gt.add_column('D')
    gt.print([])
    gt.delete_column('C')
    gt.append_row([3, 9, 1])
    gt.print([])
    print()

    # V3 - part 1
    table1 = GleanTable(['A', 'B'])
    table1.append_row([1, 3])
    table1.append_row([2, 6])
    table1.append_row([7, 9])
    table1.append_row([4, 4])
    table2 = GleanTable(['B', 'C'])
    table2.append_row([6, 8])
    table2.append_row([5, 3])
    table2.append_row([4, 2])

    inner_join_table = table1.inner_join(table2, 'B')
    inner_join_table.print([])
    print()

    # V3 - part 2
    table1_o = GleanTable(['A', 'B'])
    table1_o.append_row([1, 3])
    table1_o.append_row([2, 6])
    # table1_o.append_row([7, 4])
    # table1_o.append_row([9, 4])
    table2_o = GleanTable(['B', 'C'])
    table2_o.append_row([6, 8])
    table2_o.append_row([5, 3])
    # table2_o.append_row([4, 2])

    outer_join_table = table1_o.outer_join(table2_o , 'B')
    outer_join_table.print([])




