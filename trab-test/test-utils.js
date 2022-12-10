'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
  uploadImageBody,
  genNewUser,
  genNewUserReply,
  genUserList,
  selectUser,
  selectUserSkewed,
  genNewUpdatedUser,
  genNewAuction,
  genAuctionList,
  genAuctionsAboutToCloseList,
  genUserAuctionsList,
  genNewBid,
  genBidsList,
  genNewQuestion,
  genQuestionsList,
  genNewQuestionReply,
  random80,
  random50,
}


const Faker = require('faker/locale/en_US')
const fs = require('fs')
const path = require('path')

var imagesIds = []
var images = []
var users = []

// Auxiliary function to select an element from an array
Array.prototype.sample = function(){
	   return this[Math.floor(Math.random()*this.length)]
}

// Auxiliary function to select an element from an array
Array.prototype.sampleSkewed = function(){
	return this[randomSkewed(this.length)]
}

// Returns a random value, from 0 to val
function random( val){
	return Math.floor(Math.random() * val)
}

// Returns the user with the given id
function findUser( id){
	for( var u of users) {
		if( u.id === id)
			return u;
	}
	return null
}

// Returns a random value, from 0 to val
function randomSkewed( val){
	let beta = Math.pow(Math.sin(Math.random()*Math.PI/2),2)
	let beta_left = (beta < 0.5) ? 2*beta : 2*(1-beta);
	return Math.floor(beta_left * val)
}


// Loads data about images from disk
function loadData() {
	var basedir
	if( fs.existsSync( '/trab-test-finished/images')) 
		basedir = '/trab-test-finished/images'
	else
		basedir =  'trab-test-finished/images'	
	fs.readdirSync(basedir).forEach( file => {
		if( path.extname(file) === ".jpeg") {
			var img  = fs.readFileSync(basedir + "/" + file)
			images.push( img)
		}
	})
	var str;
	if( fs.existsSync('users.data')) {
		str = fs.readFileSync('users.data','utf8')
		users = JSON.parse(str)
	} 
}

loadData();

/**
 * Sets the body to an image, when using images.
 */
function uploadImageBody(requestParams, context, ee, next) {
	requestParams.body = images.sample()
	return next()
}

/**
 * Process reply of the download of an image. 
 * Update the next image to read.
 */
function processUploadReply(requestParams, response, context, ee, next) {
	if( typeof response.body !== 'undefined' && response.body.length > 0) {
		imagesIds.push(response.body)
	}
    return next()
}

/**
 * Select an image to download.
 */
function selectImageToDownload(context, events, done) {
	if( imagesIds.length > 0) {
		context.vars.imageId = imagesIds.sample()
	} else {
		delete context.vars.imageId
	}
	return done()
}


/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
	const first = `${Faker.name.firstName()}`
	const last = `${Faker.name.lastName()}`
	context.vars.id = first + "." + last
	context.vars.name = first + " " + last
	context.vars.nickName = first + last
	context.vars.pwd = `${Faker.internet.password()}`
	return done()
}


/**
 * Process reply for of new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let u = JSON.parse( response.body)
		users.push(u)
		fs.writeFileSync('users.data', JSON.stringify(users));
	}
    return next()
}

/**
 * List all users
 */
 function genUserList(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let u = JSON.parse( response.body)
		fs.writeFileSync('usersList.data', JSON.stringify(u));
	}
    return next()
}

/**
 * Generate data for a new user using Faker
 */
 function genNewUpdatedUser(context, events, done) {
	const first = `${Faker.name.firstName()}`
	const last = `${Faker.name.lastName()}`
	context.vars.name = first + " " + last
	context.vars.nickName = first + last
	context.vars.pwd = `${Faker.internet.password()}`
	return done()
}


/**
 * Select user
 */
function selectUser(context, events, done) {
	if( users.length > 0) {
		let user = users.sample()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}


/**
 * Select user
 */
function selectUserSkewed(context, events, done) {
	if( users.length > 0) {
		let user = users.sampleSkewed()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}

/**
 * Generate data for a new channel
 * Besides the variables for the auction, initializes the following vars:
 * numBids - number of bids to create, if batch creating 
 * numQuestions - number of questions to create, if batch creating 
 * bidValue - price for the next bid
 */
function genNewAuction(context, events, done) {
	//context.vars.title = `${Faker.commerce.productName()}`
	context.vars.idAuction = `${Faker.phone.phoneNumber()}`
	context.vars.description = `${Faker.commerce.productDescription()}`
	var priceFaker = `${Faker.commerce.price()}`
	context.vars.price = parseInt(priceFaker).toFixed(0)
	context.vars.winnerBidId = context.vars.price + random(3)
	context.vars.bidValue = context.vars.price + random(3)
	context.vars.status = "open"
	var d = new Date();
	d.setTime(Date.now() + random( 300000));
	//context.vars.endDateTime = d.toISOString().format("yyyy-MM-dd HH:mm");
	context.vars.endDateTime = "2022-12-21 10:00";
	
	return done()
}

/**
 * List all auctions
 */
 function genAuctionList(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let a = JSON.parse( response.body)
		fs.writeFileSync('auctions.data', JSON.stringify(a));
	}
    return next()
}


/**
 * List all auctions about to close
 */
 function genAuctionsAboutToCloseList(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let a = JSON.parse( response.body)
		fs.writeFileSync('auctionsAboutToClose.data', JSON.stringify(a));
	}
    return next()
}

/**
 * List all auctions about to close
 */
 function genUserAuctionsList(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let a = JSON.parse( response.body)
		fs.writeFileSync('auctionsFromUser.data', JSON.stringify(a));
	}
    return next()
}


/**
 * Generate data for a new bid
 */
function genNewBid(context, events, done) {
	if( typeof context.vars.bidValue == 'undefined') {
		if( typeof context.vars.minimumPrice == 'undefined') {
			context.vars.bidValue = parseInt(random(10)).toFixed(0)
		} else {
			context.vars.bidValue = context.vars.minimumPrice + random(2).toFixed(0)
		}
	}
	
	context.vars.idBid = `${Faker.phone.phoneNumber()}`
	context.vars.value = context.vars.bidValue;
	context.vars.bidValue = context.vars.bidValue + 1 + random(2).toFixed(0)
	return done()
}

/**
 * List all bids of an auction
 */
 function genBidsList(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let b = JSON.parse( response.body)
		fs.writeFileSync('bids.data', JSON.stringify(b));
	}
    return next()
}

/**
 * Generate data for a new question
 */
function genNewQuestion(context, events, done) {
	context.vars.idQuestion = `${Faker.phone.phoneNumber()}`
	context.vars.message = `${Faker.lorem.paragraph()}`;
	return done()
}

/**
 * List all auctions about to close
 */
 function genQuestionsList(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let q = JSON.parse( response.body)
		fs.writeFileSync('questions.data', JSON.stringify(q));
	}
    return next()
}

/**
 * Generate data for a new reply
 */
function genNewQuestionReply(context, events, done) {
	delete context.vars.reply;
	if( Math.random() > 0.5) {
		if( typeof context.vars.auctionUser !== 'undefined') {
			var user = findUser( context.vars.auctionUser);
			if( user != null) {
				context.vars.auctionUserPwd = user.pwd;
				context.vars.reply = `${Faker.lorem.paragraph()}`;
			}
		}
	} 
	return done()
}


/**
 * Return true with probability 50% 
 */
function random50(context, next) {
  const continueLooping = Math.random() < 0.5
  return next(continueLooping);
}

/**
 * Return true with probability 50% 
 */
function random80(context, next) {
  const continueLooping = Math.random() < 0.8
  return next(continueLooping);
}

/**
 * Process reply for of new users to store the id on file
 */
function extractCookie(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300)  {
		for( let header of response.rawHeaders) {
			if( header.startsWith("scc:session")) {
				context.vars.mycookie = header.split(';')[0];
			}
		}
	}
    return next()
}


