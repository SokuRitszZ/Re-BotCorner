import os
import subprocess
from subprocess import PIPE
import running_system.compiler as compiler

debug = False

class Sandbox:

  BOTS = dict()
  def __init__(self, uuid, code, lang):

    # 代码路径
    self.uuid = uuid
    self.code = code # 代码
    self.config = compiler.config[lang]

    # 存放路径
    self.cwd = '/root/'
    # 代码所在路径
    self.path_code = self.cwd + 'code-' + self.uuid + '.' + self.config['suffix']
    # 运行代码
    self.path_target = self.cwd + 'prog-' + self.uuid if self.config.get('compile_command') is not None else self.path_code
    # 输入文件
    self.path_data = self.cwd + 'data-' + self.uuid

    # 容器
    self.container = None
    # 进程
    self.proc = None
    # 编译与否
    self.compiled = False

    self.create()

  def create(self):
    if self.container:
      return 

    local_images = self._get_local_images()
    use_images = self.config['images']
    available_images = filter(lambda x: x['repo'] in use_images, local_images)
    image = ''
    if available_images:
      image = max(available_images, key=lambda x: x['tag'])
      image = image['repo'] + ':' + image['tag']
    else:
      # 拉镜像
      pass
    
    self.image = image

    result = self._run_container(self.image)
    if result is not 'ok':
      return result
    result = self._prepare_code()
    return result
  
  def compile(self):
    if self.compiled: return 'ok'
    if self.config.get('compile_command') is None: return 'ok'

    compile_command = self.config['compile_command'].format(code=self.path_code, target=self.path_target)
    command = 'docker exec %s /bin/bash -c "%s"' % (self.container, compile_command)
    p = subprocess.run(command, stderr=PIPE, shell=True)
    if p.returncode != 0:
      # 编译错误
      if debug:
        raise RuntimeError('Compile Error: %s' % p.stderr)
      else:
        return 'Compile Error: %s' % p.stderr.decode()

    self.compiled = True
    result = self._update_container(1, self.config['memory_limit'])
    return result
  
  def prepare(self, data):
    result = self._prepare_data(data)
    return result

  def run(self):
    run_command = self.config['run_command'].format(target=self.path_target, data=self.path_data)
    p = subprocess.run('docker exec %s /bin/bash -c "timeout %d %s"' % (self.container, (self.config['time_limit'] + self.config['sub_time_limit']) / 1000, run_command), stdout=PIPE, stderr=PIPE, shell=True, encoding='utf-8')
    if p.returncode != 0:
      # 运行错误
      if debug:
        raise RuntimeError("Failed to run program! Details: \n%s" % p.stderr)
      else:
        if p.stderr == None or len(p.stderr) == 0:
          return 'Time Limit Exceeded'
        return 'Runtime Error: %s' % p.stderr
    
    return p.stdout

  def stop(self):
    result = self._stop_container()
    if result is not 'ok':
      return result
    result = self._remove_container()
    return result
    
   # 获取本地镜像
  def _get_local_images(self):
    # 通过创建一个进程来运行获取镜像目录的命令，为了获取输出，需要capture_output=True, shell=True
    p = subprocess.run('docker images --no-trunc', stdout=PIPE, stderr=PIPE, shell=True, encoding='utf-8')
    
    if p.returncode != 0:
      if debug:
        raise RuntimeError('No docker installation found!')
      else:
        return list([])
    
    # 获取镜像列表
     
    output = p.stdout.strip().split('\n')
    images_list = []
    for line in output[1: ]:
      repo, tag, id, *time, size = line.split()
      images_list.append({
        'repo': repo,
        'tag': tag,
        'id': id,
        'time': ' '.join(time),
        'size': size
      })

    return images_list
  
    # 运行容器
  def _run_container(self, image):
    p = subprocess.run('docker run -itd %s /bin/bash' % image, stdout=PIPE, stderr=PIPE, shell=True, encoding='utf-8')
    if p.returncode != 0:
      # 运行容器错误
      if debug:
        raise RuntimeError('Failed to run container with image %s! Details:\n%s' % (image, p.stderr))
      else:
        return p.stderr

    self.container = p.stdout.strip()
    return 'ok'

  def _prepare_code(self):
    code_file = open(self.path_code, 'w')
    code_file.write(self.code)
    code_file.close()
    p = subprocess.run('docker cp %s %s:%s' % (self.path_code, self.container, self.path_code), stdout=PIPE, stderr=PIPE, shell=True, encoding='utf-8')
    if p.returncode != 0:
      # 复制错误
      os.remove(self.path_code)
      if debug:
        raise RuntimeError('Failed to copy code in container %s! Details:\n%s' % (self.container, p.stderr))
      else:
        return 'Prepare code error' 
    os.remove(self.path_code)
    return 'ok'
  
  def _prepare_data(self, data):
    file_data = open(self.path_data, 'w')
    file_data.write(data)
    file_data.close()
    p = subprocess.run('docker cp %s %s:%s' % (self.path_data, self.container, self.path_data), stderr=PIPE, shell=True, encoding='utf-8')
    if p.returncode != 0:
      # 复制错误
      os.remove(self.path_data)
      if debug:
        raise RuntimeError('Failed to copy data in container %s! Details:\n%s' % (self.container, p.stderr))
      else:
        return 'Prepare data error'
    os.remove(self.path_data)
    return 'ok'

  def _update_container(self, cpu, memory):
    p = subprocess.run('docker update --cpus="{cpu}" --memory="{memory}m" {container}'.format(cpu=cpu, memory=memory, container=self.container), stderr=PIPE, shell=True, encoding='utf-8')
    if p.returncode != 0:
      # 更新错误
      if debug:
        raise RuntimeError('Failed to update config of container %s! Detail:\n%s' % (self.container, p.stderr))
      else:
        return 'Update container error'
    return 'ok'
  
  def _stop_container(self):
    p = subprocess.run('docker kill %s' % self.container, stderr=PIPE, shell=True, encoding='utf-8')
    if p.returncode != 0:
      # 关闭错误
      if debug:
        raise RuntimeError('Failed to stop container %s! Details: \n%s' % (self.container, p.stderr))
      else:
        return 'Kill container error'
    return 'ok'
  
  def _remove_container(self):
    p = subprocess.run('docker rm %s' % self.container, stderr=PIPE, shell=True, encoding='utf-8')
    if p.returncode != 0:
      # 删除错误
      if debug:
        raise RuntimeError('Failed to stop container %s! Details: \n%s' % (self.container, p.stderr))
      else:
        return 'Remove container error'
    return 'ok'