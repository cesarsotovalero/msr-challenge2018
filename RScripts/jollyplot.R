library(tidyverse)
library(stringr)
library(ggExtra)

# read data
data <- read_csv(file = "DebuggingAll.txt")
data <- data.frame(data)

# select only debugging events
debugging <-
  data %>% filter(str_sub(Event, 1, 5) == "Debug") %>% select(Event)
debugging$Event <- as.factor(debugging$Event)

# count debugging events
debugging <-
  debugging %>% group_by(Event) %>% summarise(Count = n()) %>% arrange(Count) %>% mutate(LogCount = log(Count))
debugging$LogCount <- round(debugging$LogCount, digits = 2)

# order the events
debugging <- transform(debugging, Event = reorder(Event, LogCount))

# plot
theme_set(theme_bw())
plot <- debugging %>%
  filter(LogCount > 3.21) %>%
  ggplot(aes(x = Event, y = LogCount, label = LogCount)) +
  geom_point(stat = 'identity', fill = "black", size = 6)  +
  geom_segment(aes(
    y = 0,
    x = Event,
    yend = LogCount,
    xend = Event
  ),
  color = "black") +
  geom_text(color = "white", size = 2) +
  title("Logged distribution of debugging events") +
  ylim(0, 10.5) +
  coord_flip() +
  removeGrid()
plot