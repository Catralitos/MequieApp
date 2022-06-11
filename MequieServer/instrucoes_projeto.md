Nesta fase vamos usar keystores, que são ficheiros especiais que guardam chaves(públicas/privadas).

Para ser claro todos os utlizadores e o servidor vão ter keystores. Vamos ter que ter uma pasta chamada "PubKeys" que vai conter todas as chaves públicas, ou seja, todos os utilizadores têm acesso às chaves públicas uns dos outros assim como o servidor tem acesso às chaves públicas dos utilizadores e estes à chave pública do servidor.

Como as chaves públicas vão ser todas exportadas para certificados, as keystores apenas vão ter as chaves privadas.

E vai ser assim que vai ser feito, toda a gente a partilhar chaves uns com os outros.

Nesta fase vamos mesmo precisar de fazer a autenticação do utilizador, ou seja, tudo vai funcionar como dantes mas desta vez quando o cliente tentar autenticar-se o servidor vai ver se já possui o certificado do respetivo utilizador, ou seja, vai ao ficheiro que contém as chaves públicas dos utilizadores(que são certificados) e verifica se já tem o seu certificado. Se tiver, então o utilizador já se tinha registado antes, e autentica-se sem problema. Se o contrário acontecer, o utilizador autentica-se e exporta o seu certificado para a pasta dos certificados dos utilizadores(PubKeys).

A autenticação requer mais passos que isto, mas isto é resumidamente o que é preciso para a autenticação ser efetuada com sucesso.

IMPORTANTE: para a ligação entre o cliente e o servidor ser bem sucedida, vai existir uma keystore especial, que é uma trustore, que vai conter o certificado de chave pública do servidor que se vai chamar de: "trustore.client".

O socket vai ser alterado para um socket seguro TLS com autenticação unilateral.

MequieServer <port> <keystore> <keystore-password> ====> este é o novo comando para correr o servidor.

DADOS PARA INSERIR NA LINHA DE COMANDOS DO SERVIDOR:

O porto é à nossa escolha, mas quando tivermos a testar A KEYSTORE E A KEYSTORE-PASSWORD VÃO SER SEMPRE A MESMA(ESTÁ EMBAIXO OS DETALHES).

Keystore do servidor:

nome = keystoreServer
pass = 123456

o certificado do servidor é a chave pública que vai ser usada por todos os utilizadores que é:

nome = certServer.cer ===> como já tinha dito lá em cima, vai ser exportada para uma trustore

o certificado também é conhecido como trustore

Mequie <server-address> <trustore> <keystore> <keystore-password> <localUserID> ====> este é o novo comando para correr o cliente.


O server-address é à nossa escolha, mas quando tivermos a testar A KEYSTORE, A KEYSTORE-PASSWORD E O LOCALUSERID VÃO SER SEMPRE DIFERENTES DE ACORDO COM O UTILIZADOR QUE ESTÁ A SER USADO(ESTÁ EMBAIXO OS DETALHES).

trustore = trustore.client

localUserID temos de ver como vamos fazer isso => TODO!!!

Keystore do cliente:

nome = keystoreXXXXXX ====> o XXXXX vai ser o nome que dermos ao utilizador neste caso
pass = 123456 ===> a pass generica de todos os utlizadores vai ser esta, para não nos atrapalharmos

O resto dos detalhes está no enunciado, por isso, as chaves vão ser usadas para fazer todo o tipo de operações!!!

