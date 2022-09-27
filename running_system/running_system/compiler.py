config = {
  'cpp': {
    'suffix': 'cpp',
    'images': ['botcorner/cpp'],
    'time_limit': 0,
    'sub_time_limit': 1000,
    'memory_limit': 256,
    'compile_command': 'g++ -DBOTCORNER -O2 -Wall {code} -o {target}',
    'run_command': '{target} < {data}'
  },
  'python': {
    'suffix': 'py',
    'images': ['botcorner/python3'],
    'time_limit': 0,
    'sub_time_limit': 4000,
    'memory_limit': 256,
    'run_command': 'python3 {target} < {data}'
  },
  'java': {
    'suffix': 'java',
    'images': ['botcorner/java'],
    'time_limit': 0,
    'sub_time_limit': 1000,
    'memory_limit': 256,
    'compile_command': 'javac -encoding utf-8 {code}',
    'run_command': 'java -cp /root/ Main < {data}'
  },
  'javascript': {
    'suffix': 'js',
    'images': ['botcorner/javascript'],
    'time_limit': 0,
    'sub_time_limit': 2000,
    'memory_limit': 256,
    'compile_command': None,
    'run_command': 'nodejs {target} < {data}'
  },
  'typescript': {
    'suffix': 'ts',
    'images': ['botcorner/typescript'],
    'time_limit': 0,
    'sub_time_limit': 2000,
    'memory_limit': 256,
    'compile_command': 'cd & tsc {code} --out {target}',
    'run_command': 'nodejs {target} < {data}'
  }
}