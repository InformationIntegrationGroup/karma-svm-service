#Training part
library(e1071)
args<-commandArgs(TRUE)
Train<-read.csv(args[1]) #first argument URL
#Glass<-read.csv("/Users/shaarif/Desktop/ISI-Fall/Rsvm/WebContent/WEB-INF/train.csv")
numOfcol<-ncol(Train)
x<-Train[,-numOfcol]
y<-Train[,numOfcol]
kerneltype<-args[2]
#kerneltype<-"linear"
svmModel<-svm(x,y,kernel=kerneltype,type="C-classification")
#model="model.RData";
model=args[3]
model<-paste("/Users/shaarif/Desktop/ISI-Fall/Rsvm/WebContent/WEB-INF/",model,sep="")
modelName<-paste(model,'.RData',sep="")
save(svmModel, file=modelName)
modelName<-paste(modelName,"created!!",sep=" ")
modelName
summary(svmModel)