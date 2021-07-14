import kr.co.shineware.nlp.komoran.model.Token
import java.util.*

class IntendParser(private val tokens: Array<Token>) {
    fun getIoTProcotol(): IoTProtocol {
        if(tokens.filter { // 불 켜고끄기
                (it.pos == "NNG" && it.morph == "불") ||
                        (it.pos == "NNG" && it.morph == "전등") ||
                        (it.pos == "NNG" && it.morph == "조명") ||
                        (it.pos == "NNG" && it.morph == "등")
        }.isNotEmpty() &&
            tokens.filter {
                (it.pos == "VV" && it.morph == "켜") ||
                        (it.pos == "VV" && it.morph == "끄") ||
                        (it.pos == "NNB" && it.morph == "꺼") ||
                        (it.pos == "VV" && it.morph == "끌")
            }.isNotEmpty()
        ){
            val onLight = tokens.filter { // 불을 켜는가?
                    (it.pos == "VV" && it.morph == "켜")
                }.isNotEmpty()

            if( // 전체
                tokens.map { it.morph }.contains("전체") ||
                tokens.map { it.morph }.contains("전부") ||
                tokens.map { it.morph }.contains("다") ||
                tokens.map { it.morph }.contains("모든")
            ){
                return IoTProtocol("LIGHT_${if(onLight){"ON"}else{"OFF"}}", arrayOf(-1))
            }else{ // 기타
                val lightMap: Map<Int, Int> = mapOf(
                    Pair(1, 4),
                    Pair(2, 5),
                    Pair(3, 6)
                )
                val numbers = mutableListOf<Int>()
                numbers.addAll(getOrdinalNumbers().map {
                    lightMap.get(it)!!
                })

                if(tokens.filter { it.pos == "NNG" && it.morph == "거실" }.isNotEmpty()) // 거실
                    numbers.add(2) // 거실 번호

                if(tokens.filter { it.pos == "NNG" && (it.morph == "주방" || it.morph == "부엌") }.isNotEmpty()) // 주방
                    numbers.add(2) // 주방 번호

                if(tokens.filter { it.pos == "NNG" && it.morph == "화장실" }.isNotEmpty()) // 화장실
                    numbers.add(3) // 화장실 번호

                if(tokens.filter { it.pos == "NNG" && (it.morph == "현관문" || it.morph == "현관") }.isNotEmpty()) // 주방
                    numbers.add(1) // 현관문 번호

                return IoTProtocol("LIGHT_${if(onLight){"ON"}else{"OFF"}}", numbers.toTypedArray())
            }

        }else if(tokens.filter { // 문 열고닫기
                (it.pos == "NNG" && it.morph == "문")
            }.isNotEmpty() &&
            tokens.filter {
                (it.pos == "VV" && it.morph == "열") ||
                        (it.pos == "VV" && it.morph == "닫")
            }.isNotEmpty()
        ){
            val openDoor = tokens.filter { // 문을 여는가?
                (it.pos == "VV" && it.morph == "열")
            }.isNotEmpty()
            return IoTProtocol("DOOR_${if(openDoor){"OPEN"}else{"LOCK"}}", arrayOf())
        }else if(tokens.filter { // 영상 녹화하기
                (it.pos == "XSV" && it.morph == "하") ||
                        (it.pos == "VV" && it.morph == "하") ||
                        (it.pos == "VV" && it.morph == "찍")
            }.isNotEmpty() &&
            tokens.filter {
                (it.pos == "NNG" && it.morph == "녹화") ||
                        (it.pos == "NNG" && it.morph == "영상") ||
                        (it.pos == "NNG" && it.morph == "동영상")
            }.isNotEmpty()
        ){
            return IoTProtocol("CAM_RECORD", arrayOf())
        }

        return IoTProtocol("UNKNOWN", arrayOf())
    }

    private fun getOrdinalNumbers() : Array<Int> {
        val koreanNumBind = hashMapOf<String, Int>(
            Pair("첫", 1),
            Pair("둘", 2),
            Pair("두", 2),
            Pair("셋", 3),
            Pair("세", 3),
            Pair("넷", 4),
            Pair("네", 4),
            Pair("다섯", 5),
            Pair("여섯", 6),
            Pair("일곱", 7),
            Pair("여덟", 8),
            Pair("아홉", 9),
        )

        val numbers: MutableList<Int> = mutableListOf()
        for(i in 0..tokens.size - 2){
            val cur = tokens[i]

            if(arrayOf("MM", "SN").contains(cur.pos)){ // 숫자 또는 관형사인 경우
                var num = if(cur.pos == "SN") {
                    cur.morph.toInt()
                }else{
                    koreanNumBind[cur.morph]!!
                }

                numbers.add(num)

            }else if(cur.pos == "NR"){ // 수사인 경우
                val realNumStr = cur.morph.replace("째", "")
                val realNum = koreanNumBind[realNumStr]!!
                numbers.add(realNum)
            }
        }

        return numbers.toTypedArray()
    }


}