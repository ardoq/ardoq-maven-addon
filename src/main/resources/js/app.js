  $(function() {
    var vers = true;
    $("#fillerDiv").hide();
    $("#clear-form").click(function() {
      $("form").find("input#url, input#wsname, textarea").val("");
      $("form").show();
      $("#error-dialog").hide();
      $("#progress-dialog").hide();
    });

    $.url = function(sParam) {
        var sPageURL = decodeURIComponent(window.location.search.substring(1)),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === sParam) {
                return sParameterName[1] === undefined ? true : sParameterName[1];
            }
        }
    };

    var token = $.url('token');
    if(token === undefined){
        $('#token').parents('.form-group').removeClass('hidden');
    }
    else{
        $('#token').val(token);
    }

    var org = $.url('org');
    if(org === undefined){
        $('#org').parents('.form-group').removeClass('hidden');
    }
    else{
        $('#org').val(org);
    }

    var x = 0;
    var pollStatus = function(jobkey) {
        console.log("polling status");
        var body = $("body");

        $.get("/api/status/"+jobkey, function(status){
            if(status.mode === "DONE") {
                body.append('<br/><br/><div class="alert alert-success" role="alert">Done! Your Maven Project and dependencies are imported into Ardoq</div>');
                window.scrollTo(0,document.body.scrollHeight);
                return;
            }

            if(status.mode === "PENDING") {
                body.append('.');
            }

            console.log(status);
            $(status.outputBuffer).each(function(i,d){
                console.log(d);
                body.append(d).append('<br/>');
                window.scrollTo(0,document.body.scrollHeight);
            });

            window.setTimeout(function(){pollStatus(jobkey);},500);
        });

    };

    $("form#importForm").on("submit", function(e) {
        e.preventDefault();
        $("form").hide();
        $("#progress-dialog").show();

        var artifact = $('#artifactId').val();
        var workspace = $('#wsname').val();
        var organization = $('#org').val();
        var token = $('#token').val();

        var data = JSON.stringify({
                    'artifact':artifact,
                    'workspace':workspace,
                    'organization':organization,
                    'token':token
                });
            console.log(data);

        $.ajax({
            type: "POST",
            url: "/api/import",
            contentType : 'application/json',
            data : data,
            success: function(jobkey) {
                $("body").html("Importing Maven dependencies ...<br/>");
                console.log("job started: "+jobkey);
                pollStatus(jobkey);
            },
            error: function(e) {
                $("body").html("Sorry! something went wrong..");
                console.log(e);
            }
        });
        console.log("after");
    });
  });