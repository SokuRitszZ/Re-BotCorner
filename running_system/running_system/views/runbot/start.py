from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from running_system.sandbox import Sandbox

@csrf_exempt
def start(request):
  
  uuid = request.POST.get('uuid')
  code = request.POST.get('code')
  lang = request.POST.get('lang')

  if Sandbox.BOTS.get(uuid) == None:
    Sandbox.BOTS[uuid] = Sandbox(uuid, code, lang)

  return JsonResponse({
    'result': 'ok'
  })