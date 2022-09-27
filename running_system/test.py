id, rows, cols, mapStr = input().split()

id = int(id)
rows = int(rows)
cols = int(cols)
_map = [[0 for j in range(cols)] for i in range(rows)]
k = 0
for i in range(rows):
    for j in range(cols):
        _map[i][j] = mapStr[k]
        k += 1

print(1)
