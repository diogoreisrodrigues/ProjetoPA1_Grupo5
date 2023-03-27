O projeto funciona através da execução da classe "main" e da execução de várias instâncias da classe "client". Quanto aos testes unitários ao longo do projeto, vários deles
estão comentados pelo facto de vários testes unitários que envolvem ficheiros (nomeadamente bannedWords.txt assim como server.config) funcionarem quando testados na sua classe de testes
mas depois levarem a inúmeros erros após a implementação do Maven (assim como quando realizamos o package) desta forma, com o intuito de ter os javaDocs decidimos optar por
comentar a gigante maioria dos testes unitários (que antes destes erros tinham uma coverage de cerca de 60% como é observável na imagem que se encontra dentro do projeto ou através da
branch WorkingUnitTests onde é possível executá-los individualmente).