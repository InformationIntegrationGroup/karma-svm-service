library(rpart)

args <- commandArgs(TRUE)
dataset <- read.csv(args[1])
modelFile <- args[2]
#outputType <- args[3]
confusionMatrixFile <- args[4]
predictedFileName <- args[3]
load(modelFile)
pred_label <- predict(DTModel, newdata = dataset, type = 'class')
dataset['predicted'] <- pred_label
#if(outputType == 'predictions') {
	print("generating predictions")
	write.table(dataset,file=predictedFileName,sep=',',col.names=NA, quote = FALSE)
    #print(dataset)
#} else if(outputType == 'confusion_matrix') {
    # Assuming that the last column contains the actual labels
    cols <- names(dataset)
    n_cols <- length(cols)
    confusionMatrix <- table(pred = pred_label, true = dataset[, n_cols])
    print("generating confusionMatrix")
    #print(confusionMatrix)
    write.table(confusionMatrix,file=confusionMatrixFile,sep=',',col.names=NA, quote = FALSE)
    
#} else {
#    print("Wrong value for output-type!")
#}