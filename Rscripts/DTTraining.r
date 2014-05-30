library(rpart)

args <- commandArgs(TRUE)
args
dataset <- read.csv(args[1])
modelFilePath<-paste('models/',args[2],'.RData',sep="")
cols <- names(dataset)
n_cols <- length(cols)
# Assuming that the last column has the labels
fmla <- as.formula(paste(cols[n_cols], "~", paste(cols[1:n_cols-1], collapse = "+")))
DTModel <- rpart(fmla, data = dataset, method = "class")
save(DTModel, file = modelFilePath)
summary(DTModel)