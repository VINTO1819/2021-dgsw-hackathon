// initialize recognition engine
window.SpeechRecognition =
  window.SpeechRecognition || window.webkitSpeechRecognition;

const recognition = new SpeechRecognition();

recognition.interimResults = true;
let speechToText = '';
let recogRunning = false;

$('.mic').click(() => {
  $('.responseTxt').html('<br/>')
  startRecognize()
})

recognition.addEventListener('result', (e) => {
  // console.log(e);
  let interimTranscript = '';
  for (let i = e.resultIndex, len = e.results.length; i < len; i++) {
    let transcript = e.results[i][0].transcript;
    // console.log(transcript)
    if (e.results[i].isFinal) {
      speechToText += transcript;
      console.log(`Finish: ${transcript}`);
      $.ajax({
        type: 'POST',
        url: 'http://localhost/nlp',
        contentType:'text/plain;charset=UTF-8',
        dataType: 'text',
        data: transcript,
        success: (raw) => {
          var data = JSON.parse(raw);
          console.log(data);
          
          var txt = ""
          const nbind = [ "현관", "거실/부엌", "화장실",  "1번 방", "2번 방", "3번 방" ]
          const viewingList = data.payload.map(it => 
            nbind[it - 1]
          )
          switch(data.opCode){
            case 'LIGHT_ON' :
              if(data.payload.length > 0){
                if(data.payload[0] == -1){
                  txt = '집의 모든 불을 켭니다!'
                  controlLed([1,2,3,4,5,6], 'on')
                }else{
                  txt = `${viewingList.join(', ')}의 불을 켭니다`
                  controlLed(data.payload, 'on')
                }
              }else txt = '어떤 방을 켜야할 지 모르겠어요;;'
              
              
              break;
            case 'LIGHT_OFF' :
              if(data.payload.length > 0){
                if(data.payload[0] == -1){
                  txt = '집의 모든 불을 끕니다!'
                  controlLed([1,2,3,4,5,6], 'off')
                }else{
                  txt = `${viewingList.join(', ')}의 불을 끕니다`
                  controlLed(data.payload, 'off')
                }
              }else txt = '어떤 방을 꺼야할 지 모르겠어요;;'
              break;
            case 'DOOR_OPEN' : 
              txt = '문이 열립니다'
              controlServo('on')
              break;
            case 'DOOR_LOCK': 
              txt = '문이 잠깁니다'
              controlServo('off')
              break;
            case 'CAM_RECORD': 
              txt = '영상 녹화를 시작합니다'
              break;
            default :
              setTimeout(() => {
                if(!recogRunning){
                  $('.para').html(`<p class="voiceTxt">Say something<p>`);
                  $('.responseTxt').html('<br/>')
                }
              }, 3000)
              txt = '알 수 없는 명령어입니다'
              break
          }
          $('.responseTxt').text(txt)
          recogRunning = false;
        }
      })

      $('.para').html(`<p class="voiceTxt">${transcript}<p>`);
    } else {
      interimTranscript += transcript;
      $('.para').html(`<p class="voiceTxt">${interimTranscript}<p>`);
      // console.log(transcript);
    }
  }
});

function startRecognize(){
  if(!recogRunning){
    $('.para').html(`<p class="voiceTxt">듣는 중입니다..<p>`);
    recogRunning = true;
    recognition.start();
  }else{
    $('.para').html(`<p class="voiceTxt">Aborted<p>`);
    setTimeout(() => {
      if(!recogRunning){
        $('.para').html(`<p class="voiceTxt">Say something<p>`);
      }
    }, 3000)

    recogRunning = false;
    recognition.stop() 
  }
  
}

function controlLed(ledList, st){
  $.ajax({
    type: 'POST',
    url: 'http://192.168.137.116:5001/speak/led',
    contentType:'application/json;charset=UTF-8',
    dataType: 'json',
    data: JSON.stringify({
        "led_arr":ledList.join(','),
        "status": st,
        "key":"111111" 
    }),
    success: (raw) => {

    }
  })
}

function controlServo(st){
  $.ajax({
    type: 'POST',
    url: 'http://192.168.137.116:5001/speak/servo',
    contentType:'application/json;charset=UTF-8',
    dataType: 'json',
    data: JSON.stringify({
        "status": st,
        "key":"111111" 
    }),
    success: (raw) => {

    }
  })
}
