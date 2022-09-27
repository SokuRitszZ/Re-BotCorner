import json
import subprocess
import os
import compiler

class Sandbox:
  CONFIG_TIME_RATIO = 2
  BOTS = {}

  def __init__(self, code, ext):
    assert os.path.exists(code)
    assert ext in compiler.config
    self.code = code
    self.config = compiler.config[ext]
    self.cwd = '/home/user/'
    self.path_code = self.cwd + 'code.' + self.config['suffix']
    self.path_target = self.cwd + 'prog' if self.config['compile_command'] else self.path_code
    self.container = None
    self.proc = None
    self.compiled = False
    
  def create(self):
    if self.container:
      print("Warning: container already created")
      return 
    
    local_images = self._get_local_images()
    for image in self.config['images']:
      available_images = []
      for local_image in local_images:
        if local_image['repo'] == image:
          available_images.append(local_image)
      if available_images:
        break
    if available_images:
      image = max(available_images, key = lambda x: x['tag'])
      image = image['repo'] + ':' + image['tag']
    else:
      pass
      # Pull

    self._run_container(self.image)
    self._copy_to_container(self.code, self.path_code)
  
  def copy(self, folder):
    assert os.path.isdir(folder)
    self._copy_to_container(folder, '/data')
    self._prepare_userdata()

  def compile(self):
    if self.compiled: return
    if self.config['compile_command'] is None: return 

    compile_command = self.config['compile_command'].format(code = self.path_code, target = self.path_target)
    command = 'docker exec -it -u user %s /bin/bash -c "%s"' % (self.container, compile_command)
    p = subprocess.run(command, shell = True)
    if p.returncode != 0:
      raise RuntimeError('Compile error!')
    print('Compile successfully!')
    self.compiled = True
    self._update_container(1, self.config['memory_limit'])
  
  def run(self, input, keep_running = False):
    assert isinstance(input, str)
    if not self.proc:
      run_command = self.config['run_command'].format(target = self.path_target)
      wrapper_command = '~/wrapper -d {cwd} -t {tl} {stl} -m {ml} -o {ol} {command}'.format(
        cwd = self.cwd,
        tl = self.config['time_limit'] * Sandbox.CONFIG_TIME_RATIO,
        stl = '-k {tl}'.format(tl = self.config['sub_time_limit'] * Sandbox.CONFIG_TIME_RATIO) if keep_running else '',
        ml = self.config['memory_limit'],
        ol = 1048576,
        command = run_command
      )
      command = 'docker exec -i -u user %s /bin/bash -c "%s"' % (self.container, wrapper_command)
      p = subprocess.Popen(command, shell = True, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.STDOUT)
    else:
      p = self.proc
    p.stdin.write(bytearray(input, 'utf-8'))
    try:
      p.stdin.flush()
    except (OSError, BrokenPipeError):
      print('Warning: Broken pipe when flushing stdin in run')
    output = p.stdout.readline()
    output = json.loads(output)
    if output['keep_running']:
      self.proc = p
    else:
      try:
        p.communicate(timeout = 1)
      except:
        p.kill()
      self.proc = None
    return output
      
  def _get_local_images(self):
    p = subprocess.run('docker images --no-trunc', capture_output = True, shell = True, encoding = 'utf-8')
    if p.returncode != 0:
      raise RuntimeError('No docker installation found!')
    l = p.stdout.strip().split('\n')
    images = []
    for line in l[1: ]:
      repo, tag, id, *time, size = line.split()
      images.append({
        'repo': repo,
        'tag': tag,
        'id': id,
        'time': ' '.join(time),
        'size': size
      })
    return images

  def _run_container(self, image):
    p = subprocess.run('docker run -td --rm %s /bin/bash' % image, capture_output = True, shell = True, encoding = 'utf-8')
    if p.returncode != 0:
      raise RuntimeError('Failed to run container with image %s! Detail: \n%s' % (image, p.stderr))
    self.container = p.stdout.strip()
    print('Container %s created successfully' % self.container)

  def _copy_to_container(self, src, dst):
    p = subprocess.run('docker cp "%s" "%s:%s"' % (src, self.container, dst), capture_output = True, shell = True, encoding = 'utf-8')
    if (p.returncode != 0):
      raise RuntimeError('Failed to copy file %s to container %s! Details:\n%s' % (src, self.container, p.stderr))

  def _prepare_userdata(self):
    p = subprocess.run('docker exec %s /bin/ln -s /data /home/user/data' % self.container, capture_output = True, shell = True, encoding = 'utf-8')
    if p.returncode != 0:
      raise RuntimeError('Failed to create symbolic link to /data in container %s! Detail:\n%s' % (self.container, p.stderr))
    p = subprocess.run('docker exec %s /bin/chown -R user /data' % self.container)
    if p.returncode != 0:
      raise RuntimeError('Failed to chown /data to user in container %s! Detail:\n%s' % (self.container, p.stderr))

  def _update_container(self, cpu, memory):
    p = subprocess.run('docker update --cpus="{cpu}" --memory="{memory}m" --memory-swap="{memory}m" {container}'.format(cpu = cpu, memory = memory, container = self.container))
    if p.returncode != 0:
      raise RuntimeError('Failed to update config of container %s!Detail:\n%s' % (self.container, p.stderr))