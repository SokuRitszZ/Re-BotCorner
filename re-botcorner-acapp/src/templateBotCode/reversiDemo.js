export default id => {
  switch (id) {
    case 1:
      return `#include <iostream>
#include <ctime>
#include <cstdlib>
#include <vector>

using namespace std;

const int N = 20;
const int dx[]{ -1, -1, 0, 1, 1, 1, 0, -1 };
const int dy[]{ 0, 1, 1, 1, 0, -1, -1, -1 };

int chess[N][N];
int id, rows, cols;

int main() {
  string __s;
  cin >> id >> rows >> cols >> __s;
  for (int i = 0, k = 0; i < rows; ++i) {
    for (int j = 0; j < cols; ++j) {
      chess[i][j] = __s[k++] - '0';
    }
  }
  int x = 0, y = 0;

  // 在这里填写你的代码

  cout << x << ' ' << y;
  return 0;
}`;
    case 2:
      return `import java.io.*;

class Main {
  private static final int[] dx = { -1, -1, 0, 1, 1, 1, 0, -1 };
  private static final int[] dy = { 0, 1, 1, 1, 0, -1, -1, -1 };
  private static int id, rows, cols;
  private static int[][] chess;

  public static void main(String[] args) throws Exception {
    String[] params = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");
    String __s;
    id = Integer.parseInt(params[0]);
    rows = Integer.parseInt(params[1]);
    cols = Integer.parseInt(params[2]);
    __s = params[3];
    chess = new int[rows][cols];
    for (int i = 0, k = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        chess[i][j] = Integer.parseInt(String.valueOf(__s.charAt(k++)));
      }
    }
    int x = 0, y = 0;

    // 在这里填写你的代码

    System.out.println(x + " " + y);
  }
}`;
    case 3: 
      return `dx = [ -1, -1, 0, 1, 1, 1, 0, -1 ]
dy = [ 0, 1, 1, 1, 0, -1, -1, -1 ]

params = input().split(' ')
id = int(params[0])
rows = int(params[1])
cols = int(params[2])
__s = params[3]
chess = [[0 for j in range(cols)] for i in range(rows)]
k = 0
for i in range(rows):
  for j in range(cols):
    chess[i][j] = int(__s[k])
    k += 1
x = 0
y = 0

# 在这里填写你的代码

print("%d %d" % (x, y))`;
    case 4:
      return `let buf = '';
const dx = [-1, -1, 0, 1, 1, 1, 0, -1];
const dy = [0, 1, 1, 1, 0, -1, -1, -1];
process.stdin.on('readable', function () {
  let chunk = process.stdin.read();
  if (chunk) buf += chunk;
});
process.stdin.on('end', function () {
  const params = buf.split(' ');
  let p = 0;
  const id = parseInt(params[p++]);
  const rows = parseInt(params[p++]);
  const cols = parseInt(params[p++]);
  const __s = params[p++];
  let chess = [];
  for (let i = 0, k = 0; i < rows; ++i) {
    chess[i] = [];
    for (let j = 0; j < cols; ++j) {
      chess[i][j] = parseInt(__s[k++]);
    }
  }
  let x = 0, y = 0;

  // 在这里填写你的代码

  console.log(\`\${x} \${y}\`);
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