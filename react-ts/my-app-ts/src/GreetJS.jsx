export const GreetJS = () => {
  // 引数 'name' にはどんな型（文字列、数値など）でも渡せる
  const greet = (name) => {
    return "Hello, " + name + "!!";
  };

  return (
    <div>
      <p>{greet("John")}</p> {/* OK */}
      <p>{greet(42)}</p>     {/* 意図しないが、エラーにはならない */}
    </div>
  )
}