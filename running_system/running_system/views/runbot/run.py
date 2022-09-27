from django.views.decorators.csrf import csrf_exempt
from django.http import JsonResponse

from running_system.sandbox import Sandbox

@csrf_exempt
def run(request):
  uuid = request.POST.get('uuid')
  sandbox: Sandbox = Sandbox.BOTS.get(uuid)

  data = None
  if sandbox is not None:
    data = sandbox.run().strip()
  
  return JsonResponse({
    'result': 'ok',
    'data': data
  })