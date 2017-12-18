library(dplyr)
library(tidytext)
library(janeaustenr)
library(igraph)
library(ggraph)
library(widyr)

# read data
data <- read_csv(file = "DebuggingAll.txt")
data <- data.frame(data)

debugging <- data %>% select(Event)
i <- 1
for (i in 1:nrow(debugging)) {
  if (!is.na(debugging[i, 1]) &
      str_sub(debugging[i, 1], 1, 5) == "Debug") {
    debugging[i, 1] <- "DebugEvent"
  }
}

all <- debugging
all %>% filter(Event != ":" &
                 Event != "&") %>% group_by(Event) %>% summarise(Count = n())


tmp <- all %>% slice(200000:300000)

# create a ngram
austen_bigrams <-
  tmp %>% unnest_tokens(
    output = text,
    input = Event,
    token = "ngrams",
    n = 2
  )

# retrieve the most common ngrams
austen_bigrams %>% count(text, sort = TRUE)

austen_bigrams <-
  austen_bigrams %>% separate(text, c("word1", "word2"), sep = " ")

# new bigram counts
bigram_counts <- austen_bigrams %>% count(word1, word2, sort = TRUE)

# filter for only relatively common combinations
bigram_graph <- bigram_counts %>%
  filter(n > 300) %>%
  graph_from_data_frame()

# improved graph
a <- grid::arrow(type = "closed", length = unit(.07, "inches"))
ggraph(bigram_graph, layout = "fr") +
  geom_edge_link(
    aes(edge_alpha = n),
    show.legend = FALSE,
    arrow = a,
    end_cap = circle(.07, 'inches')
  ) +
  geom_node_point(color = "lightblue", size = 5) +
  geom_node_text(aes(label = name),
                 vjust = 1,
                 hjust = 1,
                 size = 2) +
  theme_void()

# pairwise correlation
word_cors <- bigram_counts %>%
  filter(n > 600) %>%
  pairwise_cor(word1, word2, sort = TRUE)

word_cors %>%
  filter(correlation > 0.5) %>%
  graph_from_data_frame() %>%
  ggraph(layout = "fr") +
  geom_edge_link(aes(edge_alpha = correlation), show.legend = FALSE) +
  geom_node_point(color = "lightblue", size = 6) +
  geom_node_text(aes(label = name), repel = TRUE) +
  theme_void()
