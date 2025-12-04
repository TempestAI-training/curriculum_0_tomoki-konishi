export const GreetTS = () => {
  // 引数 'name' は 文字列(string) だけ！と型を指定
  const greet = (name: string) => {
    return "Hello, " + name + "!!";
  };

  return (
    <div>
      <p>{greet("John")}</p> {/* OK */}
      <p>{greet("Taro")}</p>     {/* 🚨 この行でエラーが発生！ */}
    </div>
  )
}