const assignGrades = (scores) => {
    let grade;// ここに scores.map() を使った処理を記述
  // アロー関数 (score) => { ... } の中で、if 文を使って条件分岐
    grade = scores.map((score)=>{
        if (score>= 80){
            return"A";
        }
        else if (score >=60){
            return"B";
        
        }
        else if(score >=40){
        return"C";
        }
        else if(score >=20){
            return"D"

        }
        else{
            return"E"
        }
    } )
    return grade;
  // 80点以上: "A"
  // 60点以上: "B"
  // 40点以上: "C"
  // 20点以上: "D"
  // それ以外: "E"
  //
  // となるように、対応する文字列を入れる

    
  // (ヒント: return scores.map((score) => { ... }); )
};


// 1. 生徒の数を入力してもらう (N)
const N = Number(window.prompt("生徒の数を入力してください (例: 3)"));

// 2. N人分の点数を「1人ずつ」入力してもらい、配列 testScores を作成する
const testScores = [];
for (let i = 0; i < N; i++) {
    // (i + 1) を使って「1人目」「2人目」... と表示する
    const score = Number(window.prompt((i + 1) + "人目の点数を入力してください"));
    
    // 配列に .push() で score を追加
    testScores.push(score);
}


// 3. 入力された点数の配列を確認用に出力
console.log("入力された点数:", testScores);

// 4. 関数を実行し、成績の配列をコンソールに出力
console.log("成績:", assignGrades(testScores));

/* コンソールに
   (例: N=3 で、85, 95, 50 と入力した場合)
   入力された点数: [85, 95, 50]
   成績: ['A', 'A', 'C']
   のように、入力した内容に応じた結果が出れば OK
*/