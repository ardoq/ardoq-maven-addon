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
        var body = $("body");

        $.get("/api/status/"+jobkey, function(status){
            if(status.mode === "DONE") {
                body.append('<br/><br/><div class="alert alert-success" role="alert">Done! Your Maven Project and dependencies are imported into Ardoq! </div>');
                body.append('<a class="btn btn-primary btn-lg" href="'+status.workspaceURL+'" role="button" target="_parent">Open workspace</a>');
                window.scrollTo(0,document.body.scrollHeight);
                return;
            }

            if(status.mode === "PENDING") {
                body.append('.');
            }

            $(status.outputBuffer).each(function(i,d){
                body.append(d).append('<br/>');
                window.scrollTo(0,document.body.scrollHeight);
            });

            if(status.mode === "ERROR") {
                body.append('<br/><br/><div class="alert alert-danger" role="alert">Error! Something went wrong with your import. Please make sure that your artifact ID is correct, and available.</div>');
                window.scrollTo(0,document.body.scrollHeight);
                return;
            }

            window.setTimeout(function(){pollStatus(jobkey);},500);
        });

    };

    $("form#importForm").on("submit", function(e) {
        e.preventDefault();
        $("form").hide();
        $("#progress-dialog").show();

        var data = JSON.stringify({
                    'artifact':$('#artifactId').val(),
                    'workspace':$('#wsname').val(),
                    'organization':$('#org').val(),
                    'token':$('#token').val(),
                    'scope':$('input[name="scope"]:checked').val(),
                    'repoURL':$('#repoURL').val(),
                    'repoUsername':$('#repoUsername').val(),
                    'repoPassword':$('#repoPassword').val()
                });

        $.ajax({
            type: "POST",
            url: "/api/import",
            contentType : 'application/json',
            data : data,
            success: function(jobkey) {
                $("body").html("Importing Maven dependencies ...<br/>");
                pollStatus(jobkey);
            },
            error: function(e) {
                $("body").html("Sorry! something went wrong..");
            }
        });
    });
  });