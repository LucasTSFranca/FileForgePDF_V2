# FileForge / FileForgePDF

**FileForge** é uma aplicação desktop desenvolvida em **Java Swing** voltada para a conversão, mesclagem e compressão de múltiplos formatos de arquivos para o formato **PDF**. O sistema possui um design moderno com tema escuro (Dark Mode) e oferece recursos avançados como compressão sob medida e junção de documentos heterogêneos em um único arquivo PDF.

---

## 🚀 Funcionalidades

### 1. Conversão Multi-formato para PDF
O FileForge suporta a conversão individual ou em lote de diversos tipos de arquivos:
*   **Documentos de Texto (`.txt`):** Convertidos diretamente para parágrafos no PDF.
*   **Documentos do Word (`.docx`):** Convertidos mantendo a formatação original (tabelas, imagens, fontes, layouts complexos) com alta fidelidade.
*   **Planilhas e Dados Separados por Vírgula (`.csv`):** Identifica automaticamente o separador (vírgula ou ponto e vírgula) e renderiza as informações no formato de uma tabela estilizada com linhas alternadas e cabeçalho destacado.
*   **Páginas Web (`.html`, `.htm`):** Remove tags HTML e formata o conteúdo textual de forma limpa.
*   **Imagens (`.jpg`, `.jpeg`, `.png`, `.bmp`, `.gif`):** Redimensiona as imagens proporcionalmente para caberem em uma página padrão A4 e centraliza o conteúdo.

### 2. Mesclagem de Arquivos (Merge)
*   Permite selecionar múltiplos arquivos de extensões variadas (por exemplo, combinar uma imagem, um documento `.docx` e um texto `.txt`) e mesclá-los em um único documento PDF gerado em sequência.
*   Possibilidade de nomear o arquivo PDF de saída customizado.

### 3. Compressão Dinâmica e Inteligente
*   **Limitação de Tamanho:** Opção de definir um tamanho máximo de arquivo de saída em megabytes (MB).
*   **Algoritmo de Compressão Iterativo:** Caso o arquivo PDF gerado ultrapasse o tamanho desejado, a aplicação comprime as imagens (reduzindo a escala e a qualidade) em até 5 tentativas consecutivas até atingir ou ficar abaixo da meta configurada.

### 4. Interface Gráfica Moderna (Dark Theme)
*   Painel amigável estilizado em tons escuros (cinza escuro, laranja e vermelho para ações de alerta).
*   Tabela com lista de arquivos originais a serem processados.
*   Tabela com arquivos convertidos que mostra o tamanho final e a **taxa de variação de compressão (%)** de cada item (em cores para destacar redução ou aumento de tamanho).
*   Barra de progresso e status em tempo real.
*   Histórico e log de eventos integrado na parte inferior da tela.

---

## 🛠️ Tecnologias Utilizadas

*   **Java 25:** Versão mais recente da linguagem para desenvolvimento do projeto.
*   **Java Swing:** Para construção de uma interface gráfica desktop nativa e personalizada.
*   **iText (Liferay Patched):** Biblioteca principal utilizada para a criação, escrita e formatação do documento PDF final.
*   **Apache POI:** Para suporte à leitura de metadados e manipulação de arquivos do Microsoft Office.
*   **documents4j:** Framework utilizado para a conversão de alta qualidade do Word (`.docx`) para PDF, utilizando a API nativa do Word instalado localmente.

---

## 📂 Estrutura do Projeto (MVC)

O projeto está estruturado de forma limpa, seguindo conceitos de MVC (Model-View-Controller) adaptados e uso de Repositórios em memória:

```
src/main/java/
├── app/
│   └── Main.java                      # Ponto de entrada da aplicação que define o Look and Feel do sistema.
├── model/
│   ├── FileModel.java                 # Representação de metadados de arquivos originais.
│   └── FileConvertModel.java          # Representação de metadados de arquivos gerados (PDF).
├── repository/
│   ├── FileRepository.java            # Simulação de banco de dados NoSQL (lista em memória) para originais.
│   └── FileConvertRepository.java     # Simulação de banco de dados NoSQL (lista em memória) para convertidos.
├── resource/
│   └── Converter.java                 # Interface base para as classes de conversão.
├── service/
│   ├── ConvertService.java            # Serviço principal que orquestra a compressão, mesclagem e conversão.
│   ├── TxtToPdfConverter.java         # Conversor específico de arquivos .txt.
│   ├── ImageToPdfConverter.java       # Conversor específico de imagens (.jpg, .png, etc.).
│   ├── DocxToPdfConverter.java        # Conversor específico de arquivos .docx.
│   ├── CsvToPdfConverter.java         # Conversor específico de planilhas .csv.
│   └── HtmlToPdfConverter.java        # Conversor específico de páginas HTML.
└── view/
    └── MainView.java                  # Painel visual construído com Swing contendo layouts, eventos e styling.
```

---

## 📋 Pré-requisitos

Para executar este projeto em sua máquina local, você precisará de:
1.  **JDK 25** instalado e configurado no PATH do sistema.
2.  **Apache Maven** (para gerenciar dependências e build).
3.  **Microsoft Word** (instalado localmente): O framework `documents4j` requer o Word instalado na máquina para poder invocar a conversão exata do layout de arquivos `.docx` para PDF.

---

## ⚙️ Como Executar

No diretório raiz do projeto (onde está o arquivo `pom.xml`), execute o seguinte comando no terminal para compilar e iniciar a aplicação:

```bash
mvn clean compile exec:java
```
Ou utilize o executavel "FileForgePDF.exe", removendo assim a necessidade de comandos externos.
---

## 📖 Instruções de Uso

1.  **Definir Destino:** Clique no botão **"Pasta Destino"** no canto superior direito para escolher o diretório onde os PDFs gerados serão salvos.
2.  **Adicionar Arquivos:** Clique no botão **"Adicionar"** no painel da esquerda e selecione um ou mais arquivos suportados (`.txt`, `.docx`, `.csv`, `.html`, `.jpg`, `.png`, etc.).
3.  **Configurar Opções (Opcional):**
    *   Marque **"Mesclar em um único PDF"** se quiser juntar todos os arquivos da lista em um único PDF e digite o nome de saída no campo ao lado.
    *   Marque **"Limitar tamanho"** e defina o valor máximo (em MB) desejado para forçar a compressão do arquivo gerado.
4.  **Iniciar Conversão:**
    *   Selecione um arquivo da lista e clique em **"Converter Selecionado"**, ou
    *   Clique em **"Converter Todos"** para processar a lista inteira de uma vez.
5.  **Visualizar Resultados:**
    *   Os arquivos resultantes aparecerão na tabela do painel da direita (**Arquivos Convertidos**).
    *   Selecione o PDF gerado e clique em **"Abrir PDF"** para visualizá-lo diretamente no leitor de PDF do seu sistema operacional.
    *   Acompanhe os resultados da conversão e o percentual de compressão atingido, além do log de eventos na área inferior.
