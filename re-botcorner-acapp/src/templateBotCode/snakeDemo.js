export default id => {
  switch (id) {
    case 1:
      return `#include <iostream>
#include <vector>

using namespace std;

const int N = 20;
const int dx[]{ -1, 0, 1, 0 };
const int dy[]{ 0, 1, 0, -1 };

int id, rows, cols, step;
int len[2];
int g[N][N];
vector<pair<int, int>> snake[2];

int main() {
    cin >> id >> rows >> cols >> step;
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            char c;
            cin >> c;
            g[i][j] = c - '0';
        }
    }
    cin >> len[0] >> len[1];
    for (int i = 0; i < 2; ++i) {
        for (int j = 0; j < len[i]; ++j) {
            pair<int, int> p;
            cin >> p.first >> p.second;
            snake[i].push_back(p);
        }
    }
    int direction = 0;

    // 在这里填写你的代码

    cout << direction;
    return 0;
}`;
  case 2:
    return `import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Main {
  private final static int dx[] = {-1, 0, 1, 0};
  private final static int dy[] = {0, 1, 0, -1};
  static class Pair {
    int x;
    int y;
    Pair(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
  private static int id, rows, cols, step;
  private static int[] len = new int[2];
  private static int[][] g;
  private static List<Pair>[] snake = new List[2];

  public static void main(String[] args) {
    Scanner jin = new Scanner(System.in);
    id = jin.nextInt();
    rows = jin.nextInt();
    cols = jin.nextInt();
    step = jin.nextInt();
    String mapStr = jin.next();
    len[0] = jin.nextInt();
    len[1] = jin.nextInt();
    for (int i = 0; i < 2; ++i) {
      snake[i] = new ArrayList<>();
      for (int j = 0; j < len[i]; ++j) {
        int x = jin.nextInt();
        int y = jin.nextInt();
        snake[i].add(new Pair(x, y));
      }
    }
    jin.close();
    g = new int[rows][cols];
    for (int i = 0, k = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        g[i][j] = Integer.parseInt(String.valueOf(mapStr.charAt(k++)));
      }
    }
    int direction = 0;
  
    // 在这里填写你的代码
  
    System.out.println(direction);
  }
}`;
  case 3: 
    return `dx = [-1, 0, 1, 0]
dy = [0, 1, 0, -1]

params = input().split(' ')

id = int(params[0])
rows = int(params[1])
cols = int(params[2])
step = int(params[3])
mapStr = params[4]
length = [int(params[5]), int(params[6])]

snake = [[], []]
p = 7
for i in range(2):
    for j in range(length[i]):
        snake[i].append([int(params[p]), int(params[p + 1])])
        p += 2
g = [[0 for j in range(cols)] for i in range(rows)]
p = 0
for i in range(rows):
    for j in range(cols):
        g[i][j] = int(mapStr[p])
        p += 1

direction = 0

# 在这里填写你的代码

print(direction)`;
  case 4:
    return `let buf = '';
const dx = [ -1, 0, 1, 0 ];
const dy = [ 0, 1, 0, -1 ];
process.stdin.on('readable', function() {
    let chunk = process.stdin.read();
    if (chunk) buf += chunk;
});
process.stdin.on('end', function() {
    const params = buf.split(' ');
    let p = 0;
    const id = parseInt(params[p++]);
    const rows = parseInt(params[p++]);
    const cols = parseInt(params[p++]);
    const step = parseInt(params[p++]);
    const mapStr = params[p++];
    const len = [ 0, 0 ];
    len[0] = parseInt(params[p++]);
    len[1] = parseInt(params[p++]);
    let snake = [ [], [] ];
    let g = [];
    for (let i = 0, k = 0; i < rows; ++i) {
        g[i] = [];
        for (let j = 0; j < cols; ++j) {
            g[i][j] = parseInt(mapStr[k++]);
        }
    }
    for (let i = 0; i < 2; ++i) {
        for (let j = 0; j < len[i]; ++j) {
            snake[i].push([ parseInt(params[p]), parseInt(params[p + 1]) ]);
            p += 2;
        }
    }
    let direction = 0;

    // 在这里填写你的代码

    console.log(direction);
});`;
  case 5:
    return `let buf: string = '';
const dx: number[] = [ -1, 0, 1, 0 ];
const dy: number[] = [ 0, 1, 0, -1 ];
process.stdin.on('readable', function() {
    let chunk: string = process.stdin.read();
    if (chunk) buf += chunk;
});
process.stdin.on('end', function() {
    const params: string[] = buf.split(' ');
    let p: number = 0;
    const id: number = parseInt(params[p++]);
    const rows: number = parseInt(params[p++]);
    const cols: number = parseInt(params[p++]);
    const step: number = parseInt(params[p++]);
    const mapStr: string = params[p++];
    const len: number[] = [ 0, 0 ];
    len[0] = parseInt(params[p++]);
    len[1] = parseInt(params[p++]);
    let snake: any[] = [ [], [] ];
    let g: number[number[]] = [];
    for (let i = 0, k = 0; i < rows; ++i) {
        g[i] = [];
        for (let j = 0; j < cols; ++j) {
            g[i][j] = parseInt(mapStr[k++]);
        }
    }
    for (let i = 0; i < 2; ++i) {
        for (let j = 0; j < len[i]; ++j) {
            snake[i].push([ parseInt(params[p]), parseInt(params[p + 1]) ]);
            p += 2;
        }
    }
    let direction: number = 0;

    // 在这里填写你的代码

    console.log(direction);
});`;
  }
};