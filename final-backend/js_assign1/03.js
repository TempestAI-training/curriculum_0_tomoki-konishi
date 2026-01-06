const N = Number(window.prompt("自然数を入力してください"));

/* 1からNまでの「偶数の和」(evenSum) と 
  「奇数の和」(oddSum) を計算します。 */
let evenSum = 0;
let oddSum = 0;

for(let i =1;i<=N; i++){
    if(i%2==0){
        evenSum+=i;
    }
    else if(i%2==1){
        oddSum+=i
    }
}
/* ループを作成し、順番iが偶数なら(i%2 === 0　)ならevenSumにiを加えるなどします。 */

/* ２つの差分を表示
  evenSumとoddSumの差分の絶対値を出力します。絶対値はMath.abs()を使います。
  出力にはdocument.write()を使いましょう。
*/
document.write(Math.abs(oddSum-evenSum));