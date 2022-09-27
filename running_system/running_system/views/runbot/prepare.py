from django.views.decorators.csrf import csrf_exempt
from django.http import JsonResponse

from running_system.sandbox import Sandbox

@csrf_exempt
def prepare(request):
  uuid = request.POST.get('uuid')
  data = request.POST.get('data')
  
  sandbox: Sandbox = Sandbox.BOTS.get(uuid)
  if sandbox is not None:
    sandbox.prepare(data)

  return JsonResponse({
    'result': 'ok'
  })