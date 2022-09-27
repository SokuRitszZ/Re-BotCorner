config = {
  'cpp' : {
    'suffix': 'cpp',
    'images': ['botcorner/cpp'],
    'time_limit': 1000,
    'sub_time_limit': 1000,
    'memory_limit': 256,
    'compile_command': 'g++ -DBOTCORNER -O2 -Wall -mavx {code} -lpthread -o {target}',
    'run_command': '{target} < {data}'
  }
}