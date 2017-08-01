# install.packages("caret", repos='http://lib.stat.cmu.edu/R/CRAN/', dependencies=c("Depends", "Suggests"))
# library(caret)

# load libraries
library(caret)
library(mlbench)
library(randomForest)
# load dataset
data(Sonar)
set.seed(7)
Sys.sleep(10)
# create 80%/20% for training and validation datasets
validation_index <- createDataPartition(Sonar$Class, p=0.80, list=FALSE)
validation <- Sonar[-validation_index,]
training <- Sonar[validation_index,]
# create final standalone model using all training data
set.seed(7)
final_model <- randomForest(Class~., training, mtry=2, ntree=2000)
# save the model to disk
dir.create(file.path(".", "output"), showWarnings = FALSE)
saveRDS(final_model, "./output/final_model.rds")