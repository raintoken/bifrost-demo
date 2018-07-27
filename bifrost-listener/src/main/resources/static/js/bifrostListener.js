(function () {
  var params = {
    network: 'test',
    horizonURL: 'http://localhost:8000',
    bifrostURL: 'http://localhost:8800',
    horizonAllowHttp: true
  };

  var session = new Bifrost.Session(params);
  var keypair;
  session.startEthereum(onEvent).then(params => {
    keypair = params.keypair;
    document.getElementById("contributionAddress").innerText = params.address;
  }).catch(err => console.error(err));

  function onEvent(event, data) {
    if (event === Bifrost.TransactionReceivedEvent) {
      changeStatus("Transaction received");
    } else if (event === Bifrost.AccountCreatedEvent) {
      changeStatus("Account created");
    } else if (event === Bifrost.AccountConfiguredEvent) {
      changeStatus("Account configured");
    } else if (event === Bifrost.ExchangedEvent) {
      document.getElementById(
          "stellarPublicKey").innerText = keypair.publicKey();
      document.getElementById("stellarPrivateKey").innerText = keypair.secret();
    } else if (event === Bifrost.ErrorEvent) {
      console.error(data);
    }
  }

  function changeStatus(statusText) {
    document.getElementById("transactionStatus").innerText = statusText;
  }
})()