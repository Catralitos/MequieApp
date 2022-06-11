# Mequie
Repositorio para o Cliente do Mequie

INSTRUÇÕES (leiam, senão isto vai dar merda)
Para começar recomendo fazerem TODOS os tutoriais do learngitbranching.js.org que tambem tem um bom modo sandbox para perceber isto. Aprendam pelo menos sobre Git Rebase.

Para gerirmos isto de uma forma meio decente, vamos ter que usar branches, e fazer bastantes commits. Aliás o que todos os guias de git dizem, é fazer muitos branches, o mais no inicio possivel, e fazer commits frequentes, para dar rollback mais facilmente.
Logo façam branches para cada feature basicamente. é tal como quando aprendem a transformar cada coisa do código num método, em vez de poucos métodos maiores, o raciocinio é o mesmo.
Isto é para garantir que mesmo que nós trabalhamos nos mesmos ficheiros, podemos continuar a trabalhar independentemente, e depois podemos dar merge nos branches e combinar o codigo de duas versões do ficheiro numa só (o git tem um mecanismo para tratar de conflictos, em que há codigos diferentes nas mesmas partes/linhas do ficheiro, depois elaboro).

Agora vocês a este ponto já devem perceber como funciona o git merge, que é elaborado aqui em baixo: puxam de um branch para fazer uma
fusão do conteudo do branch atual, com o branch escolhido. Na nossa experiência a PSI, isso às vezes dava problemas, porque não só não
tinhamos branches suficientes, mas chegava a um ponto em que estávamos só a fazer debugging alternado, e merge de código novo geralmente
dava mais problemas. Daí, temos um novo comando para estas situações: GIT REBASE.

O git rebase, basicamente se estão num branch que saiu do master, pega em todos os commits desde esse ponto onde foi o checkout e copia/substitui no master.Só no branch em que estão a trabalhar fazer git rebase master (master é só um exemplo), e ele automaticamente copia tudo do branch que estão a trabalhar para o master, neste caso.

Isto pode parecer confuso, mas não devia, porque fizeram os tutoriais todos como disse certo? Deviam até já saber o que isto é certo? (A sério façam os tutoriais):

Então heis as instruções para trabalhar nisto (notem, se o professor der ficheiros, metam no master antes de começarem a fazer branches, fica mais simples)

  1. Ligar à VPN (obviamente)
  2. git clone https://git.alunos.di.fc.ul.pt/fc51964/projeto-de-psi (no Windows 10 deve-vos aparecer uma janela para login, no Linux acho que pede na shell)

Em ramos há duas operações: branch e checkout
git branch  cria um branch no nó/commit currente
git checkout  torna o nó/commit de nome o currente
Alternativamente podem combinar estes dois comandos com:
git checkout -b 

Todos os projetos começam com um só branch, o master.
No inicio se já nao tiverem um branch, fazem checkout do master, e criam um para a feature que vão fazer.
Para quando forem trabalhar

  3. git checkout branchXXXXX , porque cada nó da arvóre/branch representa um commit ou seja um estado do projeto, logo só podem sacar um nó de cada vez, não é como se aparecesse em pastas cada branch ou assim
  4. git pull (para terem a versão mais recente dos ficheiros) NOTA: Não podem fazer pull se têm mudanças por farem commit, sou seja, vamos supor um cenário em que estão a trabalhar num branch e vão dormir, sem dar commit, mas entretanto outra pessoas deu push nesse branch. Logo o pull vai-se queixar que vai substituir modificações que fizeram localmente. Assim têm que escolher esses ficheiros com git add e depois dar ou commit, subsituindo o que está no branch, ou stash, para descartarem as modificações para o git assim poder subsituir sem problemas. A primeira coisa que fazem quando fazem checkout de um branch é certificarem-se que têm a versão mais recente, porque se naõ tiverem, podem depois não conseguir fazer commit.
 
Em principio, não vão tocar muito no master, excepto para colocar lá uma feature já feita. Maioria do trabalho será em branches extra, que vão sendo mergidos de volta noutros branches maiores, que depois vão ser mergidos no master. Mas lembrem-se se querem dar merge do conteudo de outro branch para o que estão a trabalhar de momento, só estarem nesse branch onde vão trabalhar e darem merge com o outro branch, do qual querem o conteúdo.

De vez em quando o merge pode dar erro de conflicto, ou seja, ele não sabe que parte do código dos dois ficheiros escolher. O Sourcetree geralmente permite visualizar os tais confilcitos. Podem manualmente modificar o ficheiro para evitar o conflicto, ou podem usar duas flags quando dão merge:

-X theirs
-X ours

Quando metem um destes no final de um comando merge, estão a dizer que em caso de conflicto, escolher sempre o código deles/o outro branch (theirs) ou o código do nosso branch(ours).

Agora vamos falar do rebase outra vez. OK, para recapitular, heis quando deviam usar cada.
   MERGE: Estou a juntar uma feature já feita, e a mergir para um branch que represente um conjunto de features, ou estou a fazer um barnch para corrigir um bug, fazer uma feature, e preciso do código de outro branch para começar (em principio, não precisam para este caso, visto que vão criar um branch novo, logo será um checkout -b)
   REBASE: Retomei o progresso de outra pessoas no meu PC, e quero completamente subsitutir o que já estava naquele branch. (NOTA: Em principio, como vamos ter um branch para cada feature, isto é só dar checkout desse branch e resumir o código e dar push outra vez. O rebase em principio não vai ser necessário porque não vai haver casos em que estamos a fazer a mesma coisa em branches diferentes.) 
