#include <iostream>
#include <ctime>
#include <cstdlib>
#include <vector>

using namespace std;

const int N = 20;
const int dx[]{ -1, -1, 0, 1, 1, 1, 0, -1 };
const int dy[]{ 0, 1, 1, 1, 0, -1, -1, -1 };

int chess[N][N];
int id, rows, cols;

bool is_in(int x, int y) {
  return x >= 0 && x < rows && y >= 0 && y < cols;
}

bool is_valid_dir(int x, int y, int i) {
  int nx = x + dx[i];
  int ny = y + dy[i];
  bool flg = false;
  while (is_in(nx, ny) && chess[nx][ny] == (id ^ 1)) {
    nx += dx[i];
    ny += dy[i];
    flg = true;
  }
  return flg && is_in(nx, ny) && chess[nx][ny] == id;
}

bool is_valid(int x, int y) {
  if (!is_in(x, y) || chess[x][y] != 2) return false;
  for (int i = 0; i < 8; ++i) {
    if (is_valid_dir(x, y, i)) return true;
  }
  return false;
}

int main() {
  srand(time(0));
  string __s;
  cin >> id >> rows >> cols >> __s;
  for (int i = 0, k = 0; i < rows; ++i) {
    for (int j = 0; j < cols; ++j) {
      chess[i][j] = __s[k++] - '0';
    }
  }
  int x = 0, y = 0;

  vector<pair<int, int>> valids;
  for (int i = 0; i < rows; ++i) {
    for (int j = 0; j < cols; ++j) {
      if (is_valid(i, j)) {
        valids.emplace_back(i, j);
      }
    }
  }
  if (valids.size()) {
    pair<int, int> p = valids[(int)rand() % valids.size()];
    x = p.first, y = p.second;
  }

  cout << x << ' ' << y;
  return 0;
}