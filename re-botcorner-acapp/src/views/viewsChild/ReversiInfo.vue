<template>
  <h1 class="text-center">黑白棋</h1>
  <hr>
  <p>玩家可以落子在棋盘上，相应的也有规定。简明规定如下：</p>
  <p>1、落子的地方，必须满足在八个方向上，存在某方向是有自己的子，并且与这个子之间的路上全部都是敌子（不能没有敌子）。</p>
  <p>2、在合法的地方落子之后，会<strong>分别往八个方向上</strong>一路寻找，直到遇见第一个自己的子，如果中途有空位则不会在这方向上做任何操作；如果遇到了自己的子则停下，落子位与此子之间的敌子都会变为自己的子。</p>
  <p>3、如果棋盘上有空位但只有一种子，那么该子胜利。</p>
  <p>4、如果当前所执的子无处可下但棋盘上还有自己的子，则会跳过让对手下。</p>
  <p>5、如果棋盘填满了，那么数量多者胜利；数量相同则平局。</p>
  <p>6、如果看不懂，可以访问此处<a target="_blank" href="https://wiki.botzone.org.cn/index.php?title=Reversi">黑白棋规则</a></p>
  <a class="btn btn-primary w-100 rounded-0" target="_blank" href="https://www.yuque.com/sokuritszz/lot2s5/koxhvq">Bot代码模板</a>
</template>