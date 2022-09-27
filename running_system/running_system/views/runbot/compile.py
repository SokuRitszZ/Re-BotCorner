from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from running_system.sandbox import Sandbox

@csrf_exempt
def compile(request):
  uuid = request.POST.get('uuid')
  sandbox: Sandbox = Sandbox.BOTS.get(uuid)
  if sandbox is not None:
    result = sandbox.compile()
    if result is not 'ok':
      return JsonResponse({
        'result': result
      }) 

  return JsonResponse({
    'result': 'ok'
  })