var hdkey = require("ethereumjs-wallet/hdkey");
var bip39 = require("bip39");
var fs = require("fs");

function generateNewSeed() {
  var mnemonic = bip39.generateMnemonic();
  console.log("Mnemonic: " + mnemonic);

  var masterSeed = bip39.mnemonicToSeedHex(mnemonic);
  console.log("Master seed: " + masterSeed);

  return masterSeed;
}

function getMasterKeyPair(masterSeed) {
  var hdwallet = hdkey.fromMasterSeed(masterSeed);
  console.log("Master public key: " + hdwallet.publicExtendedKey());
  console.log("Master private key: " + hdwallet.privateExtendedKey());
}

function loadPrivateKeys(masterSeed, indexes) {
  var hdwallet = hdkey.fromMasterSeed(masterSeed);
  console.log("Master public key: " + hdwallet.publicExtendedKey());
  console.log("Master private key: " + hdwallet.privateExtendedKey());

  var wallets = [];

  for (var i = 0; i < indexes.length; i++) {

    var wallet = hdwallet.deriveChild(indexes[i]).getWallet();
    var address = "0x" + wallet.getAddress().toString("hex");
    var publicKey = wallet.getPublicKeyString();
    var privateKey = wallet.getPrivateKey().toString("hex");

    console.log("Wallet address:" + address);
    console.log("Wallet private key: " + privateKey);

    var ethWallet = {
      "address": address,
      "privateKey": privateKey,
      "publicKey": publicKey
    };

    wallets.push(ethWallet);
  }

  fs.writeFile("wallets.json", JSON.stringify(wallets, null, 2));
}
