from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

from running_system.sandbox import Sandbox
@csrf_exempt
def stop(request):
  uuid = request.POST.get('uuid')
  
  sandbox: Sandbox = Sandbox.BOTS.get(uuid)
  if sandbox is not None:
    sandbox.stop()
    del Sandbox.BOTS[uuid]
  
  return JsonResponse({
    'result': 'ok'
  })