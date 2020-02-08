<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="cz.komix.pdfsign.PDFSignerLogic"%>
<%

// vsechno resim v samostatne tride
PDFSignerLogic ps = new PDFSignerLogic();

// parametry, ktere prijdou pri odeslani podpisu
// predam je do tridy
String signatureReturned = request.getParameter("signature");
String digestReturned = request.getParameter("digest");
ps.setSignature(signatureReturned);
ps.setDigest(digestReturned);

// pokud mam podpis, tak to podepisu
// pokud nemam podpis, ziskam digest
// je to jedna operace, ktera se provede cela nebo jen z poloviny
String digestToSign = ps.getDigestAndSign();


// nasledujici kod je prevzat ze signatureTest.html, kam je
// doplneno vkladani digestu a odesilani podpisu metodou POST

%><html>
<head>
    <meta charset="utf-8">
</head>
<body onload="registerAppletStateHandler()">
<script language="JavaScript">
			function signMessage() {
				try {
					if (document.getElementById('signingApplet').isActive()) {
						document.getElementById('result').value = document.getElementById('signingApplet').signMessage(document.getElementById('entryAlias').value, document.getElementById('message2Sign').value);
						document.getElementById('message').value = document.getElementById('signingApplet').getMessage();
					} else {
						alert('Applet is loading, wait a while, and try it again!');
					}
				} catch (e) {
					alert('Applet is loading, wait a while, and try it again!');
				}
			}
			function getAvailableKeys() {
				try {
					if (document.getElementById('signingApplet').isActive()) {
						document.getElementById('result').value = document.getElementById('signingApplet').getAvailableKeys();
						document.getElementById('message').value = document.getElementById('signingApplet').getMessage();
					} else {
						alert('Applet is loading, wait a while, and try it again!');
					}
				} catch (e) {
					alert('Applet is loading, wait a while, and try it again!');
				}
			}

			function signDocuments() {
				$("#ready").hide();
				$("#singing").show();
				try {
					if (document.getElementById('signingApplet').isActive()) {
						var key = $('#key').val();
						var result = document.getElementById('signingApplet').loadKey(key);
						if ("success" != result) {
			        		showError();
			        		return;
				        }
						$('#documents tr.dokument').each(function() {
							var result = document.getElementById('signingApplet').signMessageWithLoadedKey($(this).find( "#digest" ).text());
							if ("success" != result) {
				        		showError();
				        		return;
					        }
							$(this).find( "#state" ).html();
							$(this).find( "#signature textarea" ).val(document.getElementById('signingApplet').getMessage());
						});
					} else {
						alert('Applet is loading, wait a while, and try it again!');
					}
				} catch (e) {
					alert('Applet is loading, wait a while, and try it again!');
				}
				$("#singing").hide();
				$("#ready").show();
			}

			var READY = 2;
		    function registerAppletStateHandler() {
		        // register onLoad handler if applet has
		        // not loaded yet
		        if (signingApplet.status < READY)  {
		        	signingApplet.onLoad = onLoadHandler;
		        } else if (signingApplet.status >= READY) {
		            // applet has already loaded or there
		            // was an error
		            document.getElementById("appletStatus").innerHTML = 
		              "Při startu appletu došlo k chybě. Stav: "
		               + signingApplet.status;    
		        }
		    }
		    
		    function onLoadHandler() {
		        // event handler for ready state
		        document.getElementById("appletStatus").innerHTML =
		            "Applet je nahrán.";
		        draw();
		    }

			function showError(){
	    		$("#error").text(document.getElementById('signingApplet').getMessage());
				$("#error").show();
			}
			
			function createSelectionOfKey(keys){
				$("#keySelector").empty();
				var keysArray = keys.split("\n");
				for (var i = 0; i < keysArray.length; i++){
					var key = keysArray[i];
					var items = key.split(";");
					$("#keySelector").append('<input type="radio" name="alias" onchange="$(\'#key\').val(\''+items[0]+'\')">' + $('<div/>').text((items[1])).html() + '<br/>');
				}
				$("#keySelector").show();
				$("input[name=alias]:first").prop('checked',true).trigger("change");
			}
		    
		    function signingAppletStarted() {
		    	if (document.getElementById('signingApplet').isActive()) {
		        	$("#loading").hide();
		        	$("#loadingKeys").show();
		        	if ("success" != document.getElementById('signingApplet').getAvailableKeys()) {
		        		showError();
		        		return;
			        }
					createSelectionOfKey(document.getElementById('signingApplet').getMessage());
					$("#loadingKeys").hide();
					$("#ready").show();
		    	} else {
		    		$("#error").show();
		    		$("#error").text("Při nahrávání apletu došlo k chybě.");
			    }
		    }
</script>
<script src="https://code.jquery.com/jquery-1.10.2.js"></script>
<div>
	<div id="loading">Applet se nahrává...</div>
	<div id="loadingKeys" style="display:none;">Nahrávání klíčů k podpisu...</div>
	<div id="ready" style="display:none;">Applet je připraven</div>
	<div id="singing" style="display:none;">Probíhá podepisování...</div>
	<div id="keySelector" style="display:none;"></div>
	<div id="error" style="display:none;"></div>
</div>
Vybraný klíč: <input id="key" value="" />
<input type=button OnClick="signDocuments();" value="Podepsat dokumenty" />
<form method="POST">
<table id="documents">
	<tr>
		<th>Dokument</th>
		<th>Stav</th>
		<th>Digest</th>
		<th>Podpis</th>
	</tr>
	<tr class="dokument">
		<td>1</td>
		<td id="state">Připraven k podpisu</td>
		<td id="digest"><textarea rows="3" cols="20" name="digest"><%= digestToSign %></textarea></td>
		<td id="signature"><textarea rows="3" cols="80" name="signature"></textarea></td>
	</tr>
</table>
<input type="submit" value="Odeslat" />
</form>
<applet id="signingApplet" code="amcssz.spr.web.share.signature.applet.SigningApplet" archive="signing-applet.jar" width="0" height="0" />

	<hr/>

<div id="appletStatus">Applet se nahrává</div>
Entry alias: <input id="entryAlias" value="Dusan Krizan_notoken" /><br />
CN: <input id="cn" /><br />
Certificate: <input id="certificate" /><br />
<input id="message2Sign" value="test" size="100" /><br />
<input type=button OnClick="signMessage();" value="sign" />
<input type=button OnClick="getAvailableKeys();" value="getAvailableKeys" /><br />
<input id="result" /><br />
<textarea rows="10" cols="200" id="message"></textarea><br />
</body>
</html>