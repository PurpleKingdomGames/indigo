let pkgs = import <nixpkgs> {};

in pkgs.mkShell rec {
  name = "indigodev";
  
  buildInputs = with pkgs; [
    nodejs
    yarn
    glslang
    electron
    sbt
    mill
    jdk11
  ];
}
